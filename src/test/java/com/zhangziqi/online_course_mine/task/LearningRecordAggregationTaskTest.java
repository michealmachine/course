package com.zhangziqi.online_course_mine.task;

import com.zhangziqi.online_course_mine.model.entity.Chapter;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.LearningRecord;
import com.zhangziqi.online_course_mine.model.entity.Section;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.repository.ChapterRepository;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.LearningRecordRepository;
import com.zhangziqi.online_course_mine.repository.SectionRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.RedisLearningRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LearningRecordAggregationTaskTest {

    @Mock
    private RedisLearningRecordService redisLearningRecordService;

    @Mock
    private LearningRecordRepository learningRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private SectionRepository sectionRepository;

    @InjectMocks
    private LearningRecordAggregationTask task;

    @Captor
    private ArgumentCaptor<LearningRecord> learningRecordCaptor;

    private User user;
    private Course course;
    private Chapter chapter;
    private Section section;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        user = User.builder()
                .id(1L)
                .username("testUser")
                .build();

        course = Course.builder()
                .id(2L)
                .title("测试课程")
                .build();

        chapter = Chapter.builder()
                .id(3L)
                .title("测试章节")
                .build();

        section = Section.builder()
                .id(4L)
                .title("测试小节")
                .build();
    }

    @Test
    @DisplayName("聚合学习记录")
    void testAggregateLearningRecords() {
        // 准备测试数据
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DateTimeFormatter.ISO_DATE);

        // 模拟Redis键
        Set<String> keys = new HashSet<>();
        String key1 = "learning:record:" + dateStr + ":1:2:VIDEO_WATCH";
        String key2 = "learning:record:" + dateStr + ":1:2:DOCUMENT_READ";
        keys.add(key1);
        keys.add(key2);

        when(redisLearningRecordService.getLearningRecordKeys(yesterday)).thenReturn(keys);

        // 模拟Redis数据
        Map<Object, Object> data1 = new HashMap<>();
        data1.put("totalDuration", 300);
        data1.put("chapterId", "3");
        data1.put("sectionId", "4");
        data1.put("contextData", "{\"progress\":75}");

        Map<Object, Object> data2 = new HashMap<>();
        data2.put("totalDuration", 200);
        data2.put("chapterId", "3");
        data2.put("sectionId", "4");
        data2.put("contextData", "{\"progress\":50}");

        when(redisLearningRecordService.getLearningRecordData(key1)).thenReturn(data1);
        when(redisLearningRecordService.getLearningRecordData(key2)).thenReturn(data2);

        // 模拟仓库行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(chapterRepository.findById(3L)).thenReturn(Optional.of(chapter));
        when(sectionRepository.findById(4L)).thenReturn(Optional.of(section));

        // 执行方法
        task.aggregateLearningRecords();

        // 验证Redis操作
        verify(redisLearningRecordService).getLearningRecordKeys(yesterday);
        verify(redisLearningRecordService).getLearningRecordData(key1);
        verify(redisLearningRecordService).getLearningRecordData(key2);
        verify(redisLearningRecordService).deleteLearningRecord(key1);
        verify(redisLearningRecordService).deleteLearningRecord(key2);

        // 验证仓库操作
        verify(learningRecordRepository, times(2)).save(learningRecordCaptor.capture());

        // 验证保存的记录
        List<LearningRecord> savedRecords = learningRecordCaptor.getAllValues();
        assertEquals(2, savedRecords.size());

        // 验证第一条记录
        LearningRecord record1 = savedRecords.get(0);
        assertNotNull(record1);
        assertEquals(user, record1.getUser());
        assertEquals(course, record1.getCourse());
        assertEquals(chapter, record1.getChapter());
        assertEquals(section, record1.getSection());
        assertEquals("VIDEO_WATCH", record1.getActivityType());
        assertEquals(300, record1.getDurationSeconds());
        assertEquals("{\"progress\":75}", record1.getContextData());

        // 验证第二条记录
        LearningRecord record2 = savedRecords.get(1);
        assertNotNull(record2);
        assertEquals(user, record2.getUser());
        assertEquals(course, record2.getCourse());
        assertEquals(chapter, record2.getChapter());
        assertEquals(section, record2.getSection());
        assertEquals("DOCUMENT_READ", record2.getActivityType());
        assertEquals(200, record2.getDurationSeconds());
        assertEquals("{\"progress\":50}", record2.getContextData());
    }

    @Test
    @DisplayName("聚合学习记录 - 处理无效键")
    void testAggregateLearningRecords_InvalidKey() {
        // 准备测试数据
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 模拟Redis键 - 包含无效键
        Set<String> keys = new HashSet<>();
        keys.add("learning:record:invalid_key");
        keys.add("learning:record:" + yesterday.format(DateTimeFormatter.ISO_DATE) + ":1:2:VIDEO_WATCH");

        when(redisLearningRecordService.getLearningRecordKeys(yesterday)).thenReturn(keys);

        // 模拟Redis数据
        Map<Object, Object> validData = new HashMap<>();
        validData.put("totalDuration", 300);
        validData.put("chapterId", "3");
        validData.put("sectionId", "4");
        validData.put("contextData", "{\"progress\":75}");

        when(redisLearningRecordService.getLearningRecordData(anyString())).thenReturn(validData);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(chapterRepository.findById(3L)).thenReturn(Optional.of(chapter));
        when(sectionRepository.findById(4L)).thenReturn(Optional.of(section));

        // 执行方法
        task.aggregateLearningRecords();

        // 验证仓库操作 - 只应该保存一条有效记录
        verify(learningRecordRepository, times(1)).save(any(LearningRecord.class));
    }

    @Test
    @DisplayName("聚合学习记录 - 用户或课程不存在")
    void testAggregateLearningRecords_EntityNotFound() {
        // 准备测试数据
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DateTimeFormatter.ISO_DATE);

        // 模拟Redis键
        Set<String> keys = new HashSet<>();
        String key = "learning:record:" + dateStr + ":1:2:VIDEO_WATCH";
        keys.add(key);

        when(redisLearningRecordService.getLearningRecordKeys(yesterday)).thenReturn(keys);

        // 模拟Redis数据
        Map<Object, Object> data = new HashMap<>();
        data.put("totalDuration", 300);
        data.put("chapterId", "3");
        data.put("sectionId", "4");
        data.put("contextData", "{\"progress\":75}");

        when(redisLearningRecordService.getLearningRecordData(key)).thenReturn(data);

        // 模拟用户不存在
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行方法
        task.aggregateLearningRecords();

        // 验证Redis操作
        verify(redisLearningRecordService).getLearningRecordKeys(yesterday);
        verify(redisLearningRecordService).getLearningRecordData(key);

        // 验证仓库操作 - 不应该保存任何记录
        verify(learningRecordRepository, never()).save(any(LearningRecord.class));
        // Redis键不应该被删除，因为处理失败
        verify(redisLearningRecordService, never()).deleteLearningRecord(key);
    }
}
