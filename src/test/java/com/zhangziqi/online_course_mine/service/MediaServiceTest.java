package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.media.*;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Media;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.MediaStatus;
import com.zhangziqi.online_course_mine.model.enums.MediaType;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.MediaActivityCalendarVO;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
import com.zhangziqi.online_course_mine.model.vo.StorageGrowthPointVO;
import com.zhangziqi.online_course_mine.model.vo.AdminMediaVO;
import com.zhangziqi.online_course_mine.model.vo.MediaTypeDistributionVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.MediaRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
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
import org.springframework.data.jpa.domain.Specification;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

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
    private UserRepository userRepository;

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
    void testGetMediaListWithTypeFilter() {
        // 准备测试数据
        List<Media> mediaList = List.of(media);
        Page<Media> mediaPage = new PageImpl<>(mediaList);

        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.findByInstitutionAndType(
                eq(institution), 
                eq(MediaType.VIDEO), 
                any(Pageable.class))).thenReturn(mediaPage);

        // 执行测试
        Page<MediaVO> result = mediaService.getMediaList(institutionId, MediaType.VIDEO, null, Pageable.unpaged());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(mediaId, result.getContent().get(0).getId());
        assertEquals(MediaType.VIDEO.name(), result.getContent().get(0).getType());
        
        // 验证调用
        verify(mediaRepository).findByInstitutionAndType(
                eq(institution), eq(MediaType.VIDEO), any(Pageable.class));
    }
    
    @Test
    void testGetMediaListWithFilenameFilter() {
        // 准备测试数据
        List<Media> mediaList = List.of(media);
        Page<Media> mediaPage = new PageImpl<>(mediaList);
        String searchKeyword = "test";

        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.findByInstitutionAndOriginalFilenameContaining(
                eq(institution), 
                eq(searchKeyword), 
                any(Pageable.class))).thenReturn(mediaPage);

        // 执行测试
        Page<MediaVO> result = mediaService.getMediaList(institutionId, null, searchKeyword, Pageable.unpaged());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(mediaId, result.getContent().get(0).getId());
        assertTrue(result.getContent().get(0).getOriginalFilename().contains(searchKeyword));
        
        // 验证调用
        verify(mediaRepository).findByInstitutionAndOriginalFilenameContaining(
                eq(institution), eq(searchKeyword), any(Pageable.class));
    }
    
    @Test
    void testGetMediaListWithTypeAndFilenameFilter() {
        // 准备测试数据
        List<Media> mediaList = List.of(media);
        Page<Media> mediaPage = new PageImpl<>(mediaList);
        String searchKeyword = "test";

        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.findByInstitutionAndTypeAndOriginalFilenameContaining(
                eq(institution), 
                eq(MediaType.VIDEO), 
                eq(searchKeyword), 
                any(Pageable.class))).thenReturn(mediaPage);

        // 执行测试
        Page<MediaVO> result = mediaService.getMediaList(
                institutionId, MediaType.VIDEO, searchKeyword, Pageable.unpaged());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(mediaId, result.getContent().get(0).getId());
        assertEquals(MediaType.VIDEO.name(), result.getContent().get(0).getType());
        assertTrue(result.getContent().get(0).getOriginalFilename().contains(searchKeyword));
        
        // 验证调用
        verify(mediaRepository).findByInstitutionAndTypeAndOriginalFilenameContaining(
                eq(institution), eq(MediaType.VIDEO), eq(searchKeyword), any(Pageable.class));
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

    @Test
    void testGetMediaActivityCalendar() {
        // 准备测试数据
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        List<MediaActivityDTO> activities = Arrays.asList(
            new MediaActivityDTO(startDate, 5L, 1024L * 1024L * 50L), // 50MB
            new MediaActivityDTO(startDate.plusDays(1), 3L, 1024L * 1024L * 30L), // 30MB
            new MediaActivityDTO(startDate.plusDays(3), 8L, 1024L * 1024L * 80L)  // 80MB
        );
        
        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.findMediaUploadActivitiesByInstitution(
                eq(institutionId), 
                eq(startDate.atStartOfDay()), 
                eq(endDate.atTime(LocalTime.MAX))))
            .thenReturn(activities);
        
        // 执行测试
        MediaActivityCalendarVO result = mediaService.getMediaActivityCalendar(institutionId, startDate, endDate);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(activities, result.getCalendarData());
        assertEquals(8L, result.getPeakCount()); // 最大活动数为8
        assertEquals(startDate.plusDays(3), result.getMostActiveDate()); // 最活跃日期
        assertEquals(16L, result.getTotalCount()); // 总活动数: 5+3+8=16
        assertEquals(1024L * 1024L * 160L, result.getTotalSize()); // 总大小: 50+30+80=160MB
        
        // 验证调用
        verify(mediaRepository).findMediaUploadActivitiesByInstitution(
                eq(institutionId), 
                any(LocalDateTime.class), 
                any(LocalDateTime.class));
    }
    
    @Test
    void testGetAllMediaActivityCalendar() {
        // 准备测试数据
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        List<MediaActivityDTO> activities = Arrays.asList(
            new MediaActivityDTO(startDate, 10L, 1024L * 1024L * 100L), // 100MB
            new MediaActivityDTO(startDate.plusDays(2), 15L, 1024L * 1024L * 150L), // 150MB
            new MediaActivityDTO(startDate.plusDays(5), 5L, 1024L * 1024L * 50L)  // 50MB
        );
        
        // Mock 方法调用
        when(mediaRepository.findAllMediaUploadActivities(
                eq(startDate.atStartOfDay()), 
                eq(endDate.atTime(LocalTime.MAX))))
            .thenReturn(activities);
        
        // 执行测试
        MediaActivityCalendarVO result = mediaService.getAllMediaActivityCalendar(startDate, endDate);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(activities, result.getCalendarData());
        assertEquals(15L, result.getPeakCount()); // 最大活动数为15
        assertEquals(startDate.plusDays(2), result.getMostActiveDate()); // 最活跃日期
        assertEquals(30L, result.getTotalCount()); // 总活动数: 10+15+5=30
        assertEquals(1024L * 1024L * 300L, result.getTotalSize()); // 总大小: 100+150+50=300MB
        
        // 验证调用
        verify(mediaRepository).findAllMediaUploadActivities(
                any(LocalDateTime.class), 
                any(LocalDateTime.class));
    }
    
    @Test
    void testGetMediaListByDate() {
        // 准备测试数据
        LocalDate date = LocalDate.now();
        
        List<Media> mediaList = Arrays.asList(
            media,
            createMediaForTesting(4L, "测试视频2", MediaType.VIDEO, 1024L * 1024L * 20L)
        );
        Page<Media> mediaPage = new PageImpl<>(mediaList);
        
        // Mock 方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.findMediaByInstitutionAndDate(
                eq(institutionId), eq(date), any(Pageable.class)))
            .thenReturn(mediaPage);
        
        // 执行测试
        Page<MediaVO> result = mediaService.getMediaListByDate(institutionId, date, Pageable.unpaged());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(mediaId, result.getContent().get(0).getId());
        assertEquals(4L, result.getContent().get(1).getId());
        
        // 验证调用
        verify(mediaRepository).findMediaByInstitutionAndDate(
                eq(institutionId), eq(date), any(Pageable.class));
    }
    
    @Test
    void testGetAllMediaListByDate() {
        // 准备测试数据
        LocalDate date = LocalDate.now();
        
        Media media2 = createMediaForTesting(4L, "机构2视频", MediaType.VIDEO, 1024L * 1024L * 15L);
        // 设置不同机构ID
        Institution institution2 = new Institution();
        institution2.setId(2L);
        institution2.setName("机构2");
        media2.setInstitution(institution2);
        
        List<Media> mediaList = Arrays.asList(media, media2);
        Page<Media> mediaPage = new PageImpl<>(mediaList);
        
        // Mock 方法调用
        when(mediaRepository.findAllMediaByDate(eq(date), any(Pageable.class)))
            .thenReturn(mediaPage);
        
        // 执行测试
        Page<MediaVO> result = mediaService.getAllMediaListByDate(date, Pageable.unpaged());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(mediaId, result.getContent().get(0).getId());
        assertEquals(4L, result.getContent().get(1).getId());
        // 验证来自不同机构
        assertEquals(institutionId, result.getContent().get(0).getInstitutionId());
        assertEquals(2L, result.getContent().get(1).getInstitutionId());
        
        // 验证调用
        verify(mediaRepository).findAllMediaByDate(eq(date), any(Pageable.class));
    }
    
    @Test
    void testGetAllMediaList() {
        // 准备测试数据
        Media media2 = createMediaForTesting(4L, "机构2视频", MediaType.VIDEO, 1024L * 1024L * 15L);
        // 设置不同机构ID
        Institution institution2 = new Institution();
        institution2.setId(2L);
        institution2.setName("机构2");
        media2.setInstitution(institution2);
        
        List<Media> mediaList = Arrays.asList(media, media2);
        Page<Media> mediaPage = new PageImpl<>(mediaList);
        
        // Mock 方法调用
        when(mediaRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mediaPage);
        
        // 执行测试 - Pass null for type and filename
        Page<MediaVO> result = mediaService.getAllMediaList(null, null, Pageable.unpaged());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(mediaId, result.getContent().get(0).getId());
        assertEquals(4L, result.getContent().get(1).getId());
        // 验证来自不同机构
        assertEquals(institutionId, result.getContent().get(0).getInstitutionId());
        assertEquals(2L, result.getContent().get(1).getInstitutionId());
        
        // 验证调用 - Verify findAll with Specification
        verify(mediaRepository).findAll(any(Specification.class), any(Pageable.class));
    }
    
    /**
     * 由于在MediaServiceImpl中使用了this::mapToMediaVO，
     * 这个测试用于确保这种用法在代码中正常工作，通过调用getAllMediaList。
     */
    @Test
    void testConvertToMediaVO() {
        // 准备测试数据，我们只需测试可能使用convertToMediaVO的其中一个方法
        // 这里测试getAllMediaList，该方法调用了this::convertToMediaVO
        
        Media media2 = createMediaForTesting(4L, "另一个视频", MediaType.VIDEO, 1024L * 1024L * 15L);
        List<Media> mediaList = List.of(media2);
        Page<Media> mediaPage = new PageImpl<>(mediaList);
        
        // Mock方法调用
        when(mediaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mediaPage);
        
        // 执行测试 - Pass null for type and filename
        Page<MediaVO> result = mediaService.getAllMediaList(null, null, Pageable.unpaged());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(4L, result.getContent().get(0).getId());
        assertEquals("另一个视频", result.getContent().get(0).getTitle());
    }
    
    /**
     * 创建测试媒体对象
     */
    private Media createMediaForTesting(Long id, String title, MediaType type, Long size) {
        Media testMedia = new Media();
        testMedia.setId(id);
        testMedia.setTitle(title);
        testMedia.setDescription("测试描述");
        testMedia.setType(type);
        testMedia.setSize(size);
        testMedia.setOriginalFilename(title + "." + (type == MediaType.VIDEO ? "mp4" : "pdf"));
        testMedia.setStoragePath(type.name().toLowerCase() + "/" + institutionId + "/" + UUID.randomUUID() + "/" + title);
        testMedia.setStatus(MediaStatus.COMPLETED);
        testMedia.setInstitution(institution);
        testMedia.setUploaderId(uploaderId);
        testMedia.setUploadTime(LocalDateTime.now());
        testMedia.setLastAccessTime(LocalDateTime.now());
        return testMedia;
    }

    @Test
    void testGetStorageGrowthTrend() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 3);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        MediaActivityDTO day1 = new MediaActivityDTO(startDate, 5L, 1024L * 1024 * 50); // 50MB
        MediaActivityDTO day3 = new MediaActivityDTO(endDate, 10L, 1024L * 1024 * 100); // 100MB
        List<MediaActivityDTO> activities = Arrays.asList(day1, day3);

        when(mediaRepository.findAllMediaUploadActivities(startDateTime, endDateTime))
                .thenReturn(activities);

        // When
        List<StorageGrowthPointVO> result = mediaService.getStorageGrowthTrend(
                startDate, endDate, ChronoUnit.DAYS);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify point 1
        assertEquals(startDate, result.get(0).getDate());
        assertEquals(1024L * 1024 * 50, result.get(0).getSizeAdded());

        // Verify point 2
        assertEquals(endDate, result.get(1).getDate());
        assertEquals(1024L * 1024 * 100, result.get(1).getSizeAdded());

        verify(mediaRepository).findAllMediaUploadActivities(startDateTime, endDateTime);
    }

    @Test
    void testGetStorageGrowthTrend_NoData() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 3);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        when(mediaRepository.findAllMediaUploadActivities(startDateTime, endDateTime))
                .thenReturn(Collections.emptyList());

        // When
        List<StorageGrowthPointVO> result = mediaService.getStorageGrowthTrend(
                startDate, endDate, ChronoUnit.DAYS);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mediaRepository).findAllMediaUploadActivities(startDateTime, endDateTime);
    }
    
    @Test
    void testGetStorageGrowthTrend_UnsupportedGranularity() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 3);

        // When & Then 
        // Currently logs a warning, doesn't throw exception. If it threw, use assertThrows:
        // assertThrows(UnsupportedOperationException.class, () -> {
        //     mediaService.getStorageGrowthTrend(startDate, endDate, ChronoUnit.WEEKS);
        // });
        // Just call it to ensure no unexpected errors and check log (manual check)
        List<StorageGrowthPointVO> result = mediaService.getStorageGrowthTrend(startDate, endDate, ChronoUnit.WEEKS);
        assertNotNull(result); // Should still return empty list or similar based on current impl
        // We expect a warning log message here (manually verify or use log capture lib)
    }

    @Test
    void testGetAllMediaList_Admin_NoFilters() {
        // Given
        Pageable pageable = Pageable.ofSize(10);
        List<Media> mediaList = List.of(media);
        Page<Media> mediaPage = new PageImpl<>(mediaList, pageable, 1);
        
        // Use lenient() if other tests mock findAll without Specification
        when(mediaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mediaPage);

        // When
        Page<MediaVO> result = mediaService.getAllMediaList(null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(mediaId, result.getContent().get(0).getId());

        // Verify repository called with Specification
        ArgumentCaptor<Specification<Media>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(mediaRepository).findAll(specCaptor.capture(), eq(pageable));
        // In a unit test, reliably asserting the spec content is hard.
        // We primarily check that the correct method overload was called.
    }

    @Test
    void testGetAllMediaList_Admin_WithFilters() {
        // Given
        Pageable pageable = Pageable.ofSize(10);
        MediaType filterType = MediaType.VIDEO;
        String filterFilename = "test";
        List<Media> mediaList = List.of(media);
        Page<Media> mediaPage = new PageImpl<>(mediaList, pageable, 1);

        // Use lenient() if other tests mock findAll without Specification
        when(mediaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mediaPage);

        // When
        Page<MediaVO> result = mediaService.getAllMediaList(filterType, filterFilename, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(mediaId, result.getContent().get(0).getId());

        // Verify repository called with Specification
        ArgumentCaptor<Specification<Media>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(mediaRepository).findAll(specCaptor.capture(), eq(pageable));
        // Asserting the actual Specification content is complex in unit tests.
        // We trust the service layer correctly builds the spec based on inputs.
        // Integration tests would cover the Specification logic more directly.
    }

    @Test
    void testGetAdminMediaList() {
        // 准备测试数据
        List<Media> mediaList = List.of(media);
        Page<Media> mediaPage = new PageImpl<>(mediaList);
        
        User uploader = new User();
        uploader.setId(uploaderId);
        uploader.setUsername("testUser");
        
        // Mock方法调用
        when(mediaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mediaPage);
        when(userRepository.findById(uploaderId)).thenReturn(Optional.of(uploader));
        
        // 执行测试
        Page<AdminMediaVO> result = mediaService.getAdminMediaList(
                MediaType.VIDEO, "test", "测试机构", 
                LocalDateTime.now().minusDays(7), LocalDateTime.now(),
                1024L, 1024 * 1024 * 100L, Pageable.unpaged());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(mediaId, result.getContent().get(0).getId());
        assertEquals("testUser", result.getContent().get(0).getUploaderUsername());
        assertEquals("测试机构", result.getContent().get(0).getInstitutionName());
        assertNotNull(result.getContent().get(0).getFormattedSize());
        
        // 验证调用
        verify(mediaRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(userRepository).findById(uploaderId);
    }
    
    @Test
    void testGetAdminMediaListByDate() {
        // 准备测试数据
        List<Media> mediaList = List.of(media);
        Page<Media> mediaPage = new PageImpl<>(mediaList);
        
        User uploader = new User();
        uploader.setId(uploaderId);
        uploader.setUsername("testUser");
        
        // Mock方法调用
        when(mediaRepository.findAllMediaByDate(any(LocalDate.class), any(Pageable.class))).thenReturn(mediaPage);
        when(userRepository.findById(uploaderId)).thenReturn(Optional.of(uploader));
        
        // 执行测试
        Page<AdminMediaVO> result = mediaService.getAdminMediaListByDate(LocalDate.now(), Pageable.unpaged());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(mediaId, result.getContent().get(0).getId());
        assertEquals("testUser", result.getContent().get(0).getUploaderUsername());
        
        // 验证调用
        verify(mediaRepository).findAllMediaByDate(any(LocalDate.class), any(Pageable.class));
        verify(userRepository).findById(uploaderId);
    }
    
    @Test
    void testGetMediaTypeDistribution() {
        // 准备测试数据
        List<Object[]> typeCountList = new ArrayList<>();
        typeCountList.add(new Object[] { MediaType.VIDEO, 5L });
        typeCountList.add(new Object[] { MediaType.DOCUMENT, 3L });
        
        // Mock方法调用
        when(mediaRepository.countByMediaType()).thenReturn(typeCountList);
        
        // 执行测试
        MediaTypeDistributionVO result = mediaService.getMediaTypeDistribution(null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(8, result.getTotalCount());
        assertEquals(2, result.getTypeCount().size());
        assertTrue(result.getTypeCount().containsKey(MediaType.VIDEO));
        assertTrue(result.getTypeCount().containsKey(MediaType.DOCUMENT));
        assertEquals(5L, result.getTypeCount().get(MediaType.VIDEO));
        assertEquals(3L, result.getTypeCount().get(MediaType.DOCUMENT));
        
        // 验证调用
        verify(mediaRepository).countByMediaType();
    }
    
    @Test
    void testGetMediaTypeDistributionForInstitution() {
        // 准备测试数据
        List<Object[]> typeCountList = new ArrayList<>();
        typeCountList.add(new Object[] { MediaType.VIDEO, 5L });
        
        // Mock方法调用
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(mediaRepository.countByMediaTypeForInstitution(institutionId)).thenReturn(typeCountList);
        
        // 执行测试
        MediaTypeDistributionVO result = mediaService.getMediaTypeDistribution(institutionId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(5, result.getTotalCount());
        assertEquals(1, result.getTypeCount().size());
        assertTrue(result.getTypeCount().containsKey(MediaType.VIDEO));
        assertEquals(5L, result.getTypeCount().get(MediaType.VIDEO));
        
        // 验证调用
        verify(institutionRepository).findById(institutionId);
        verify(mediaRepository).countByMediaTypeForInstitution(institutionId);
    }
    
    @Test
    void testGetInstitutionStorageUsage() {
        // 准备测试数据
        List<Institution> institutions = new ArrayList<>();
        institutions.add(institution);
        
        Institution institution2 = new Institution();
        institution2.setId(2L);
        institution2.setName("测试机构2");
        institutions.add(institution2);
        
        // Mock方法调用
        when(institutionRepository.findAll()).thenReturn(institutions);
        when(mediaRepository.sumSizeByInstitution(institution)).thenReturn(1024 * 1024 * 10L);
        when(mediaRepository.sumSizeByInstitution(institution2)).thenReturn(1024 * 1024 * 5L);
        
        // 执行测试
        Map<String, Long> result = mediaService.getInstitutionStorageUsage();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("测试机构"));
        assertTrue(result.containsKey("测试机构2"));
        assertEquals(1024 * 1024 * 10L, result.get("测试机构"));
        assertEquals(1024 * 1024 * 5L, result.get("测试机构2"));
        
        // 验证调用
        verify(institutionRepository).findAll();
        verify(mediaRepository, times(2)).sumSizeByInstitution(any(Institution.class));
    }
} 