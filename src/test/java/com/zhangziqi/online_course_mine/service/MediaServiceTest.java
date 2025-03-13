package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.media.*;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Media;
import com.zhangziqi.online_course_mine.model.enums.MediaStatus;
import com.zhangziqi.online_course_mine.model.enums.MediaType;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.MediaRepository;
import com.zhangziqi.online_course_mine.service.impl.MediaServiceImpl;
import com.zhangziqi.online_course_mine.service.impl.S3MultipartUploadManager;
import com.zhangziqi.online_course_mine.service.impl.UploadStatusService;
import com.zhangziqi.online_course_mine.service.MinioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private StorageQuotaService storageQuotaService;

    @Mock
    private S3MultipartUploadManager s3UploadManager;

    @Mock
    private UploadStatusService uploadStatusService;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private MediaServiceImpl mediaService;

    private Institution institution;
    private Media media;
    private MediaUploadInitDTO uploadInitDTO;
    private UploadStatusInfo uploadStatusInfo;
    private Long institutionId = 1L;
    private Long uploaderId = 2L;
    private Long mediaId = 3L;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        institution = new Institution();
        institution.setId(institutionId);
        institution.setName("测试机构");

        media = new Media();
        media.setId(mediaId);
        media.setTitle("测试视频");
        media.setDescription("测试描述");
        media.setType(MediaType.VIDEO);
        media.setSize(1024 * 1024 * 10L); // 10MB
        media.setOriginalFilename("test.mp4");
        media.setStoragePath("video/1/123/test.mp4");
        media.setStatus(MediaStatus.UPLOADING);
        media.setInstitution(institution);
        media.setUploaderId(uploaderId);
        media.setUploadTime(LocalDateTime.now());
        media.setLastAccessTime(LocalDateTime.now());

        uploadInitDTO = new MediaUploadInitDTO();
        uploadInitDTO.setTitle("测试视频");
        uploadInitDTO.setDescription("测试描述");
        uploadInitDTO.setFilename("test.mp4");
        uploadInitDTO.setContentType("video/mp4");
        uploadInitDTO.setFileSize(1024 * 1024 * 10L); // 10MB
        uploadInitDTO.setChunkSize((int) (1024 * 1024 * 2L)); // 2MB

        // 计算总分片数
        int totalParts = (int) ((1024 * 1024 * 10L) / (1024 * 1024 * 2L));
        if ((1024 * 1024 * 10L) % (1024 * 1024 * 2L) != 0) {
            totalParts++;
        }

        uploadStatusInfo = UploadStatusInfo.builder()
                .mediaId(mediaId)
                .institutionId(institutionId)
                .uploaderId(uploaderId)
                .uploadId("test-upload-id")
                .objectKey("video/1/123/test.mp4")
                .filename("test.mp4")
                .contentType("video/mp4")
                .fileSize(1024 * 1024 * 10L)
                .status(MediaStatus.UPLOADING)
                .totalParts(totalParts)
                .completedParts(new ArrayList<>())
                .initiatedAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }

    @Test
    void testInitiateUpload() {
        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(storageQuotaService.hasEnoughQuota(eq(institutionId), any(QuotaType.class), anyLong())).thenReturn(true);
        when(mediaRepository.save(any(Media.class))).thenAnswer(i -> {
            Media m = i.getArgument(0);
            m.setId(mediaId);
            return m;
        });
        when(s3UploadManager.initiateMultipartUpload(anyString(), anyString())).thenReturn("test-upload-id");
        when(s3UploadManager.batchGeneratePresignedUrls(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(new PresignedUrlInfo(1, "https://test-url.com")));

        // 执行测试
        UploadInitiationVO result = mediaService.initiateUpload(uploadInitDTO, institutionId, uploaderId);

        // 验证结果
        assertNotNull(result);
        assertEquals(mediaId, result.getMediaId());
        assertEquals("test-upload-id", result.getUploadId());
        assertEquals(5, result.getTotalParts());
        assertEquals(1, result.getPresignedUrls().size());

        // 验证调用
        verify(mediaRepository).save(any(Media.class));
        verify(uploadStatusService).saveUploadStatus(any(UploadStatusInfo.class));
        verify(storageQuotaService, times(1)).updateUsedQuota(eq(institutionId), any(QuotaType.class), anyLong());
    }

    @Test
    void testCompleteUpload() {
        // 准备测试数据
        for (int i = 1; i <= 5; i++) {
            uploadStatusInfo.getCompletedParts().add(new UploadStatusInfo.PartInfo(i, "test-etag-" + i));
        }
        
        CompleteUploadDTO dto = new CompleteUploadDTO();
        dto.setUploadId("test-upload-id");
        // 创建完成分片列表
        List<CompleteUploadDTO.PartInfo> completedParts = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            CompleteUploadDTO.PartInfo partInfo = new CompleteUploadDTO.PartInfo();
            partInfo.setPartNumber(i);
            partInfo.setETag("test-etag-" + i);
            completedParts.add(partInfo);
        }
        dto.setCompletedParts(completedParts);

        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.findByIdAndInstitution(mediaId, institution)).thenReturn(Optional.of(media));
        when(s3UploadManager.completeMultipartUpload(anyString(), anyString(), anyList()))
                .thenReturn(CompleteMultipartUploadResponse.builder().build());
        when(mediaRepository.save(any(Media.class))).thenReturn(media);

        // 执行测试
        MediaVO result = mediaService.completeUpload(mediaId, institutionId, dto);

        // 验证结果
        assertNotNull(result);
        assertEquals(mediaId, result.getId());
        assertEquals("测试视频", result.getTitle());

        // 验证调用
        verify(mediaRepository).save(any(Media.class));
        verify(uploadStatusService).deleteUploadStatus(mediaId);
    }

    @Test
    void testGetMediaAccessUrl() {
        // 准备测试数据
        media.setStatus(MediaStatus.COMPLETED);

        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.findByIdAndInstitution(mediaId, institution)).thenReturn(Optional.of(media));
        when(s3UploadManager.generatePresignedGetUrl(anyString(), anyLong())).thenReturn("https://test-access-url.com");

        // 执行测试
        String result = mediaService.getMediaAccessUrl(mediaId, institutionId, 60L);

        // 验证结果
        assertNotNull(result);
        assertEquals("https://test-access-url.com", result);

        // 验证调用
        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    void testGetMediaList() {
        // 准备测试数据
        List<Media> mediaList = List.of(media);
        Page<Media> mediaPage = new PageImpl<>(mediaList);

        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.findByInstitution(eq(institution), any(Pageable.class))).thenReturn(mediaPage);

        // 执行测试
        Page<MediaVO> result = mediaService.getMediaList(institutionId, Pageable.unpaged());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(mediaId, result.getContent().get(0).getId());
    }

    @Test
    void testCancelUpload() {
        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.findByIdAndInstitution(mediaId, institution)).thenReturn(Optional.of(media));
        when(uploadStatusService.getUploadStatusOrNull(mediaId)).thenReturn(uploadStatusInfo);
        
        // 执行测试
        mediaService.cancelUpload(mediaId, institutionId);
        
        // 验证调用
        verify(s3UploadManager).abortMultipartUpload(anyString(), anyString());
        verify(uploadStatusService).deleteUploadStatus(mediaId);
        verify(storageQuotaService).updateUsedQuota(eq(institutionId), any(QuotaType.class), eq(-media.getSize()));
        verify(mediaRepository).delete(media);
    }

    @Test
    void testDeleteMedia() {
        // 准备测试数据
        media.setStatus(MediaStatus.COMPLETED);
        
        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.findByIdAndInstitution(mediaId, institution)).thenReturn(Optional.of(media));
        when(minioService.deleteFile(anyString())).thenReturn(true);
        
        // 执行测试
        mediaService.deleteMedia(mediaId, institutionId);
        
        // 验证调用
        verify(minioService).deleteFile(media.getStoragePath());
        verify(storageQuotaService).updateUsedQuota(eq(institutionId), any(QuotaType.class), eq(-media.getSize()));
        verify(mediaRepository).delete(media);
    }
} 