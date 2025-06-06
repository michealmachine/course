package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.LearningProgressUpdateDTO;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.enums.CoursePaymentType;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.model.enums.OrderStatus;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.vo.ChapterVO;
import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.SectionVO;
import com.zhangziqi.online_course_mine.model.vo.UserCourseVO;
import com.zhangziqi.online_course_mine.repository.ChapterRepository;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.LearningRecordRepository;
import com.zhangziqi.online_course_mine.repository.OrderRepository;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.UserCourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCourseServiceTest {

    @Mock
    private UserCourseRepository userCourseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private LearningRecordRepository learningRecordRepository;

    @InjectMocks
    private UserCourseServiceImpl userCourseService;

    private User testUser;
    private Course testCourse;
    private Order testOrder;
    private UserCourse testUserCourse;
    private Institution testInstitution;

    @BeforeEach
    void setUp() {
        // 创建测试机构
        testInstitution = Institution.builder()
                .id(1L)
                .name("测试机构")
                .build();

        // 创建测试用户
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        // 创建测试课程
        testCourse = Course.builder()
                .id(1L)
                .title("测试课程")
                .description("这是一个测试课程")
                .institution(testInstitution)
                .creatorId(1L)
                .status(CourseStatus.PUBLISHED.ordinal())
                .paymentType(CoursePaymentType.PAID.ordinal())
                .price(BigDecimal.valueOf(99.99))
                .publishedVersionId(1L)
                .studentCount(10)
                .build();

        // 创建测试订单
        testOrder = Order.builder()
                .id(1L)
                .orderNo("TEST12345678")
                .user(testUser)
                .course(testCourse)
                .amount(BigDecimal.valueOf(99.99))
                .status(OrderStatus.PAID.ordinal())
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();

        // 创建测试用户课程关系
        testUserCourse = UserCourse.builder()
                .id(1L)
                .user(testUser)
                .course(testCourse)
                .purchasedAt(LocalDateTime.now())
                .order(testOrder)
                .progress(0)
                .status(UserCourseStatus.NORMAL.ordinal())
                .learnDuration(0)
                .build();
    }

    @Test
    @DisplayName("获取用户购买的课程 - 成功")
    void getUserPurchasedCourses_Success() {
        // 准备测试数据
        when(userCourseRepository.findByUser_IdAndStatus(anyLong(), anyInt())).thenReturn(List.of(testUserCourse));

        // 执行方法
        List<UserCourseVO> result = userCourseService.getUserPurchasedCourses(testUser.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserCourse.getId(), result.get(0).getId());
        assertEquals(testUser.getId(), result.get(0).getUserId());
        assertEquals(testCourse.getId(), result.get(0).getCourseId());

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndStatus(testUser.getId(), UserCourseStatus.NORMAL.getValue());
    }

    @Test
    @DisplayName("分页获取用户购买的课程 - 成功")
    void getUserPurchasedCoursesWithPagination_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserCourse> userCoursePage = new PageImpl<>(List.of(testUserCourse), pageable, 1);

        when(userCourseRepository.findByUser_IdAndStatus(anyLong(), anyInt(), any(Pageable.class))).thenReturn(userCoursePage);

        // 执行方法
        Page<UserCourseVO> result = userCourseService.getUserPurchasedCourses(testUser.getId(), pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUserCourse.getId(), result.getContent().get(0).getId());
        assertEquals(testUser.getId(), result.getContent().get(0).getUserId());
        assertEquals(testCourse.getId(), result.getContent().get(0).getCourseId());

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndStatus(testUser.getId(), UserCourseStatus.NORMAL.getValue(), pageable);
    }

    @Test
    @DisplayName("获取用户课程学习记录 - 成功")
    void getUserCourseRecord_Success() {
        // 准备测试数据
        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.of(testUserCourse));

        // 执行方法
        UserCourseVO result = userCourseService.getUserCourseRecord(testUser.getId(), testCourse.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testUserCourse.getId(), result.getId());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testCourse.getId(), result.getCourseId());
        assertEquals(testUserCourse.getProgress(), result.getProgress());
        assertEquals(testUserCourse.getStatus(), result.getStatus());

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
    }

    @Test
    @DisplayName("获取用户课程学习记录 - 未购买课程")
    void getUserCourseRecord_CourseNotPurchased() {
        // 准备测试数据
        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userCourseService.getUserCourseRecord(testUser.getId(), testCourse.getId()));

        assertTrue(exception.getMessage().contains("未找到学习记录，请先购买课程"));

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
    }

    @Test
    @DisplayName("更新学习进度 - 成功")
    void updateLearningProgress_Success() {
        // 准备测试数据
        testUserCourse.setProgress(30);
        LearningProgressUpdateDTO dto = LearningProgressUpdateDTO.builder()
                .chapterId(1L)
                .sectionId(1L)
                .sectionProgress(50)
                .build();

        // 确保课程有章节和小节信息
        ChapterVO chapterVO = new ChapterVO();
        chapterVO.setId(1L);

        SectionVO section = new SectionVO();
        section.setId(1L);

        List<SectionVO> sections = new ArrayList<>();
        sections.add(section);
        chapterVO.setSections(sections);

        List<ChapterVO> chaptersVO = new ArrayList<>();
        chaptersVO.add(chapterVO);

        // 创建对应的实体类型
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        chapter.setEstimatedMinutes(30); // 设置预计学习时长为30分钟

        Section sectionEntity = new Section();
        sectionEntity.setId(1L);

        List<Section> sectionEntities = new ArrayList<>();
        sectionEntities.add(sectionEntity);
        chapter.setSections(sectionEntities);

        List<Chapter> chapters = new ArrayList<>();
        chapters.add(chapter);
        testCourse.setChapters(chapters);

        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.of(testUserCourse));
        when(userCourseRepository.save(any(UserCourse.class))).thenReturn(testUserCourse);
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(chapter));
        when(learningRecordRepository.getChapterLearningDuration(anyLong(), anyLong(), anyLong())).thenReturn(1200); // 返回20分钟的学习时长，未超过预计时长

        // 执行方法
        UserCourseVO result = userCourseService.updateLearningProgress(testUser.getId(), testCourse.getId(), dto);

        // 验证结果
        assertNotNull(result);
        assertEquals(dto.getSectionProgress(), result.getCurrentSectionProgress());
        assertNotNull(testUserCourse.getLastLearnAt());

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
        verify(userCourseRepository).save(testUserCourse);
    }

    @Test
    @DisplayName("更新学习进度 - 复习模式")
    void updateLearningProgress_ReviewingMode() {
        // 准备测试数据
        testUserCourse.setProgress(50);
        LearningProgressUpdateDTO dto = LearningProgressUpdateDTO.builder()
                .chapterId(1L)
                .sectionId(1L)
                .sectionProgress(100)
                .isReviewing(true) // 设置为复习模式
                .build();

        // 创建对应的实体类型
        Chapter chapter = new Chapter();
        chapter.setId(1L);

        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.of(testUserCourse));
        when(userCourseRepository.save(any(UserCourse.class))).thenReturn(testUserCourse);
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(chapter));

        // 执行方法
        UserCourseVO result = userCourseService.updateLearningProgress(testUser.getId(), testCourse.getId(), dto);

        // 验证结果
        assertNotNull(result);
        assertEquals(50, result.getProgress()); // 复习模式下进度不应该被更新

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
        verify(chapterRepository).findById(dto.getChapterId());
        verify(userCourseRepository).save(testUserCourse);
    }

    @Test
    @DisplayName("更新学习进度 - 超过预设学习时长")
    void updateLearningProgress_ExceedsEstimatedTime() {
        // 准备测试数据
        testUserCourse.setProgress(50);
        LearningProgressUpdateDTO dto = LearningProgressUpdateDTO.builder()
                .chapterId(1L)
                .sectionId(1L)
                .sectionProgress(100)
                .build();

        // 创建对应的实体类型
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        chapter.setEstimatedMinutes(30); // 设置预计学习时长为30分钟

        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.of(testUserCourse));
        when(userCourseRepository.save(any(UserCourse.class))).thenReturn(testUserCourse);
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(chapter));
        when(learningRecordRepository.getChapterLearningDuration(anyLong(), anyLong(), anyLong())).thenReturn(2400); // 返回40分钟的学习时长，超过预计时长

        // 执行方法
        UserCourseVO result = userCourseService.updateLearningProgress(testUser.getId(), testCourse.getId(), dto);

        // 验证结果
        assertNotNull(result);
        assertEquals(50, result.getProgress()); // 超过预设时长时进度不应该被更新

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
        verify(chapterRepository).findById(dto.getChapterId());
        verify(learningRecordRepository).getChapterLearningDuration(testUser.getId(), testCourse.getId(), dto.getChapterId());
        verify(userCourseRepository).save(testUserCourse);
    }

    @Test
    @DisplayName("更新学习进度 - 新进度小于原进度")
    void updateLearningProgress_NewProgressLessThanCurrent() {
        // 准备测试数据
        testUserCourse.setProgress(50);
        LearningProgressUpdateDTO dto = LearningProgressUpdateDTO.builder()
                .chapterId(1L)
                .sectionId(1L)
                .sectionProgress(30)
                .build();

        // 创建对应的实体类型
        Chapter chapter = new Chapter();
        chapter.setId(1L);

        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.of(testUserCourse));
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(chapter));

        // 执行方法
        UserCourseVO result = userCourseService.updateLearningProgress(testUser.getId(), testCourse.getId(), dto);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUserCourse.getProgress(), result.getProgress()); // 进度不应该被更新

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
        verify(chapterRepository).findById(dto.getChapterId());
        verify(userCourseRepository).save(testUserCourse);
    }

    @Test
    @DisplayName("更新学习进度 - 进度超出范围")
    void updateLearningProgress_ProgressOutOfRange() {
        // 准备测试数据
        LearningProgressUpdateDTO dto = LearningProgressUpdateDTO.builder()
                .chapterId(1L)
                .sectionId(1L)
                .sectionProgress(110) // 超过100
                .build();

        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCourseService.updateLearningProgress(testUser.getId(), testCourse.getId(), dto));

        assertTrue(exception.getMessage().contains("学习进度必须在0-100之间"));

        // 验证方法调用
        verify(userCourseRepository, never()).findByUser_IdAndCourse_Id(anyLong(), anyLong());
        verify(userCourseRepository, never()).save(any(UserCourse.class));
    }

    @Test
    @DisplayName("记录学习时长 - 成功")
    void recordLearningDuration_Success() {
        // 准备测试数据
        testUserCourse.setLearnDuration(100); // 当前学习时长100秒
        Integer additionalDuration = 60; // 新增60秒

        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.of(testUserCourse));
        when(userCourseRepository.save(any(UserCourse.class))).thenReturn(testUserCourse);

        // 执行方法
        UserCourseVO result = userCourseService.recordLearningDuration(testUser.getId(), testCourse.getId(), additionalDuration);

        // 验证结果
        assertNotNull(result);
        assertEquals(160, result.getLearnDuration()); // 100 + 60 = 160
        assertNotNull(testUserCourse.getLastLearnAt());

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
        verify(userCourseRepository).save(testUserCourse);
    }

    @Test
    @DisplayName("记录学习时长 - 时长无效")
    void recordLearningDuration_InvalidDuration() {
        // 准备测试数据
        Integer invalidDuration = 0; // 无效时长

        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCourseService.recordLearningDuration(testUser.getId(), testCourse.getId(), invalidDuration));

        assertTrue(exception.getMessage().contains("学习时长必须大于0"));

        // 验证方法调用
        verify(userCourseRepository, never()).findByUser_IdAndCourse_Id(anyLong(), anyLong());
        verify(userCourseRepository, never()).save(any(UserCourse.class));
    }

    @Test
    @DisplayName("检查用户是否已购买课程 - 已购买")
    void hasPurchasedCourse_CourseIsPurchased() {
        // 准备测试数据
        when(userCourseRepository.existsByUser_IdAndCourse_IdAndStatus(anyLong(), anyLong(), anyInt())).thenReturn(true);

        // 执行方法
        boolean result = userCourseService.hasPurchasedCourse(testUser.getId(), testCourse.getId());

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(userCourseRepository).existsByUser_IdAndCourse_IdAndStatus(testUser.getId(), testCourse.getId(), UserCourseStatus.NORMAL.getValue());
    }

    @Test
    @DisplayName("检查用户是否已购买课程 - 未购买")
    void hasPurchasedCourse_CourseNotPurchased() {
        // 准备测试数据
        when(userCourseRepository.existsByUser_IdAndCourse_IdAndStatus(anyLong(), anyLong(), anyInt())).thenReturn(false);

        // 执行方法
        boolean result = userCourseService.hasPurchasedCourse(testUser.getId(), testCourse.getId());

        // 验证结果
        assertFalse(result);

        // 验证方法调用
        verify(userCourseRepository).existsByUser_IdAndCourse_IdAndStatus(testUser.getId(), testCourse.getId(), UserCourseStatus.NORMAL.getValue());
    }

    @Test
    @DisplayName("获取课程学生列表 - 成功")
    void getCourseStudents_Success() {
        // 准备测试数据
        when(userCourseRepository.findByCourse_Id(anyLong())).thenReturn(List.of(testUserCourse));

        // 执行方法
        List<UserCourseVO> result = userCourseService.getCourseStudents(testCourse.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserCourse.getId(), result.get(0).getId());
        assertEquals(testUser.getId(), result.get(0).getUserId());
        assertEquals(testCourse.getId(), result.get(0).getCourseId());

        // 验证方法调用
        verify(userCourseRepository).findByCourse_Id(testCourse.getId());
    }

    @Test
    @DisplayName("分页获取课程学生列表 - 成功")
    void getCourseStudentsWithPagination_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserCourse> userCoursePage = new PageImpl<>(List.of(testUserCourse), pageable, 1);

        when(userCourseRepository.findByCourse_Id(anyLong(), any(Pageable.class))).thenReturn(userCoursePage);

        // 执行方法
        Page<UserCourseVO> result = userCourseService.getCourseStudents(testCourse.getId(), pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUserCourse.getId(), result.getContent().get(0).getId());
        assertEquals(testUser.getId(), result.getContent().get(0).getUserId());
        assertEquals(testCourse.getId(), result.getContent().get(0).getCourseId());

        // 验证方法调用
        verify(userCourseRepository).findByCourse_Id(testCourse.getId(), pageable);
    }

    @Test
    @DisplayName("获取机构学生列表 - 成功")
    void getInstitutionStudents_Success() {
        // 准备测试数据
        when(userCourseRepository.findByInstitutionId(anyLong())).thenReturn(List.of(testUserCourse));

        // 执行方法
        List<UserCourseVO> result = userCourseService.getInstitutionStudents(testInstitution.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserCourse.getId(), result.get(0).getId());
        assertEquals(testUser.getId(), result.get(0).getUserId());
        assertEquals(testCourse.getId(), result.get(0).getCourseId());

        // 验证方法调用
        verify(userCourseRepository).findByInstitutionId(testInstitution.getId());
    }

    @Test
    @DisplayName("分页获取机构学生列表 - 成功")
    void getInstitutionStudentsWithPagination_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserCourse> userCoursePage = new PageImpl<>(List.of(testUserCourse), pageable, 1);

        when(userCourseRepository.findByInstitutionId(anyLong(), any(Pageable.class))).thenReturn(userCoursePage);

        // 执行方法
        Page<UserCourseVO> result = userCourseService.getInstitutionStudents(testInstitution.getId(), pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUserCourse.getId(), result.getContent().get(0).getId());
        assertEquals(testUser.getId(), result.getContent().get(0).getUserId());
        assertEquals(testCourse.getId(), result.getContent().get(0).getCourseId());

        // 验证方法调用
        verify(userCourseRepository).findByInstitutionId(testInstitution.getId(), pageable);
    }

    @Test
    @DisplayName("获取用户最近学习的课程 - 成功")
    void getRecentLearnedCourses_Success() {
        // 准备测试数据
        when(userCourseRepository.findRecentLearnedCourses(anyLong(), any(Pageable.class)))
            .thenReturn(List.of(testUserCourse));

        // 执行方法
        List<CourseVO> result = userCourseService.getRecentLearnedCourses(testUser.getId(), 5);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCourse.getId(), result.get(0).getId());
        assertEquals(testCourse.getTitle(), result.get(0).getTitle());

        // 验证方法调用
        verify(userCourseRepository).findRecentLearnedCourses(eq(testUser.getId()), any(Pageable.class));
    }

    @Test
    @DisplayName("创建用户课程关系 - 成功")
    void createUserCourseRelation_Success() {
        // 准备测试数据
        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(userCourseRepository.save(any(UserCourse.class))).thenReturn(testUserCourse);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // 执行方法
        UserCourse result = userCourseService.createUserCourseRelation(testUser.getId(), testCourse.getId(), testOrder.getId(), true);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testCourse, result.getCourse());
        assertEquals(testOrder, result.getOrder());
        assertEquals(UserCourseStatus.NORMAL.ordinal(), result.getStatus());

        // 验证课程学生数增加
        assertEquals(11, testCourse.getStudentCount()); // 原来是10，增加1个

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
        verify(userRepository).findById(testUser.getId());
        verify(courseRepository).findById(testCourse.getId());
        verify(orderRepository).findById(testOrder.getId());
        verify(userCourseRepository).save(any(UserCourse.class));
        verify(courseRepository).save(testCourse);
    }

    @Test
    @DisplayName("创建用户课程关系 - 关系已存在")
    void createUserCourseRelation_RelationAlreadyExists() {
        // 准备测试数据
        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.of(testUserCourse));

        // 执行方法
        UserCourse result = userCourseService.createUserCourseRelation(testUser.getId(), testCourse.getId(), testOrder.getId(), true);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUserCourse, result);

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
        verify(userRepository, never()).findById(anyLong());
        verify(courseRepository, never()).findById(anyLong());
        verify(orderRepository, never()).findById(anyLong());
        verify(userCourseRepository, never()).save(any(UserCourse.class));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("创建用户课程关系 - 已退款课程重新购买")
    void createUserCourseRelation_RefundedCourseRebuy() {
        // 准备测试数据
        UserCourse refundedUserCourse = UserCourse.builder()
                .id(1L)
                .user(testUser)
                .course(testCourse)
                .purchasedAt(LocalDateTime.now().minusDays(7)) // 一周前购买
                .order(testOrder)
                .progress(50) // 有一些学习进度
                .status(UserCourseStatus.REFUNDED.ordinal()) // 已退款状态
                .learnDuration(3600) // 有一些学习时长
                .build();

        Order newOrder = Order.builder()
                .id(2L)
                .orderNo("NEW12345678")
                .user(testUser)
                .course(testCourse)
                .amount(BigDecimal.valueOf(99.99))
                .status(OrderStatus.PAID.ordinal())
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();

        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.of(refundedUserCourse));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(newOrder));
        when(userCourseRepository.save(any(UserCourse.class))).thenReturn(refundedUserCourse);

        // 执行方法
        UserCourse result = userCourseService.createUserCourseRelation(testUser.getId(), testCourse.getId(), newOrder.getId(), true);

        // 验证结果
        assertNotNull(result);
        assertEquals(UserCourseStatus.NORMAL.ordinal(), result.getStatus()); // 状态应该更新为正常
        assertEquals(newOrder, result.getOrder()); // 订单应该更新为新订单
        assertNotNull(result.getPurchasedAt()); // 购买时间应该更新

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
        verify(orderRepository).findById(newOrder.getId());
        verify(userCourseRepository).save(refundedUserCourse);
        verify(userRepository, never()).findById(anyLong());
        verify(courseRepository, never()).findById(anyLong());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("创建用户课程关系 - 免费课程无订单")
    void createUserCourseRelation_FreeCourseNoOrder() {
        // 准备测试数据
        when(userCourseRepository.findByUser_IdAndCourse_Id(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(userCourseRepository.save(any(UserCourse.class))).thenReturn(testUserCourse);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // 执行方法
        UserCourse result = userCourseService.createUserCourseRelation(testUser.getId(), testCourse.getId(), null, true);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testCourse, result.getCourse());
        assertNull(result.getOrder());
        assertEquals(UserCourseStatus.NORMAL.ordinal(), result.getStatus());

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(testUser.getId(), testCourse.getId());
        verify(userRepository).findById(testUser.getId());
        verify(courseRepository).findById(testCourse.getId());
        verify(orderRepository, never()).findById(anyLong());
        verify(userCourseRepository).save(any(UserCourse.class));
        verify(courseRepository).save(testCourse);
    }

    @Test
    @DisplayName("更新用户课程关系为已退款 - 成功")
    void updateUserCourseRefunded_Success() {
        // 准备测试数据
        when(userCourseRepository.findByOrder_Id(anyLong())).thenReturn(Optional.of(testUserCourse));
        when(userCourseRepository.save(any(UserCourse.class))).thenReturn(testUserCourse);
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // 执行方法
        UserCourse result = userCourseService.updateUserCourseRefunded(testOrder.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(UserCourseStatus.REFUNDED.ordinal(), result.getStatus());

        // 验证课程学生数减少
        assertEquals(9, testCourse.getStudentCount()); // 原来是10，减少1个

        // 验证方法调用
        verify(userCourseRepository).findByOrder_Id(testOrder.getId());
        verify(userCourseRepository).save(testUserCourse);
        verify(courseRepository).findById(testCourse.getId());
        verify(courseRepository).save(testCourse);
    }

    @Test
    @DisplayName("更新用户课程关系为已退款 - 关系不存在")
    void updateUserCourseRefunded_RelationNotFound() {
        // 准备测试数据
        when(userCourseRepository.findByOrder_Id(anyLong())).thenReturn(Optional.empty());

        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userCourseService.updateUserCourseRefunded(999L));

        assertTrue(exception.getMessage().contains("未找到与订单关联的课程记录"));

        // 验证方法调用
        verify(userCourseRepository).findByOrder_Id(999L);
        verify(userCourseRepository, never()).save(any(UserCourse.class));
        verify(courseRepository, never()).findById(anyLong());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("根据订单ID查找用户课程关系 - 成功")
    void findByOrderId_Success() {
        // 准备测试数据
        when(userCourseRepository.findByOrder_Id(anyLong())).thenReturn(Optional.of(testUserCourse));

        // 执行方法
        Optional<UserCourse> result = userCourseService.findByOrderId(testOrder.getId());

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(testUserCourse, result.get());

        // 验证方法调用
        verify(userCourseRepository).findByOrder_Id(testOrder.getId());
    }

    @Test
    @DisplayName("根据订单ID查找用户课程关系 - 不存在")
    void findByOrderId_NotFound() {
        // 准备测试数据
        when(userCourseRepository.findByOrder_Id(anyLong())).thenReturn(Optional.empty());

        // 执行方法
        Optional<UserCourse> result = userCourseService.findByOrderId(999L);

        // 验证结果
        assertFalse(result.isPresent());

        // 验证方法调用
        verify(userCourseRepository).findByOrder_Id(999L);
    }

    @Test
    @DisplayName("分页查询用户指定状态的课程关系 - 成功")
    void findByUserIdAndStatus_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        Integer status = UserCourseStatus.NORMAL.ordinal();
        Page<UserCourse> userCoursePage = new PageImpl<>(List.of(testUserCourse), pageable, 1);

        when(userCourseRepository.findByUser_IdAndStatus(anyLong(), anyInt(), any(Pageable.class)))
                .thenReturn(userCoursePage);

        // 执行方法
        Page<UserCourse> result = userCourseService.findByUserIdAndStatus(testUser.getId(), status, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUserCourse.getId(), result.getContent().get(0).getId());
        assertEquals(testUserCourse.getUser().getId(), result.getContent().get(0).getUser().getId());
        assertEquals(testUserCourse.getCourse().getId(), result.getContent().get(0).getCourse().getId());
        assertEquals(status, result.getContent().get(0).getStatus());

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndStatus(testUser.getId(), status, pageable);
    }

    @Test
    @DisplayName("分页查询用户指定状态的课程关系 - 无记录")
    void findByUserIdAndStatus_NoRecords() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        Integer status = UserCourseStatus.REFUNDED.ordinal();
        Page<UserCourse> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(userCourseRepository.findByUser_IdAndStatus(anyLong(), anyInt(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // 执行方法
        Page<UserCourse> result = userCourseService.findByUserIdAndStatus(testUser.getId(), status, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndStatus(testUser.getId(), status, pageable);
    }
}