package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.QuestionGroupDTO;
import com.zhangziqi.online_course_mine.model.dto.QuestionGroupItemDTO;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.vo.QuestionGroupItemVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionGroupVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionVO;
import com.zhangziqi.online_course_mine.repository.*;
import com.zhangziqi.online_course_mine.service.impl.QuestionGroupServiceImpl;
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionGroupServiceTest {

    @Mock
    private QuestionGroupRepository groupRepository;

    @Mock
    private QuestionGroupItemRepository groupItemRepository;

    @Mock
    private SectionQuestionGroupRepository sectionGroupRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private QuestionService questionService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuestionGroupServiceImpl questionGroupService;

    private Institution testInstitution;
    private User testUser;
    private QuestionGroup testGroup;
    private Question testQuestion;
    private QuestionGroupItem testGroupItem;
    private QuestionGroupDTO testGroupDTO;
    private QuestionGroupItemDTO testGroupItemDTO;
    private QuestionVO testQuestionVO;
    private List<Object[]> testQuestionCountResult;

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
                .name("测试用户")
                .username("testuser")
                .nickname("测试昵称")
                .email("test@example.com")
                .build();

        // 创建测试题目组
        testGroup = QuestionGroup.builder()
                .id(1L)
                .institution(testInstitution)
                .name("测试题目组")
                .description("这是一个测试题目组")
                .creatorId(testUser.getId())
                .build();

        // 创建测试题目
        testQuestion = Question.builder()
                .id(1L)
                .institution(testInstitution)
                .title("测试题目")
                .content("这是一道测试题目")
                .type(1)
                .difficulty(2)
                .score(5)
                .build();

        // 创建测试题目组项
        testGroupItem = QuestionGroupItem.builder()
                .id(1L)
                .group(testGroup)
                .question(testQuestion)
                .orderIndex(0)
                .difficulty(2)
                .score(5)
                .build();

        // 创建测试题目组DTO
        testGroupDTO = QuestionGroupDTO.builder()
                .id(1L)
                .institutionId(testInstitution.getId())
                .name("测试题目组")
                .description("这是一个测试题目组")
                .build();

        // 创建测试题目组项DTO
        testGroupItemDTO = QuestionGroupItemDTO.builder()
                .id(1L)
                .groupId(testGroup.getId())
                .questionId(testQuestion.getId())
                .orderIndex(0)
                .difficulty(2)
                .score(5)
                .build();

        // 创建测试题目VO
        testQuestionVO = QuestionVO.builder()
                .id(testQuestion.getId())
                .institutionId(testInstitution.getId())
                .title(testQuestion.getTitle())
                .content(testQuestion.getContent())
                .type(testQuestion.getType())
                .difficulty(testQuestion.getDifficulty())
                .score(testQuestion.getScore())
                .options(new ArrayList<>())
                .build();

        // 创建测试计数结果
        testQuestionCountResult = new ArrayList<>();
        testQuestionCountResult.add(new Object[]{testGroup.getId(), 5L});
    }

    @Test
    @DisplayName("创建题目组 - 成功")
    void createGroup_Success() {
        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(groupRepository.save(any(QuestionGroup.class))).thenReturn(testGroup);

        // 执行测试
        QuestionGroupVO result = questionGroupService.createGroup(testGroupDTO, testUser.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testGroup.getId(), result.getId());
        assertEquals(testGroup.getName(), result.getName());
        assertEquals(testGroup.getDescription(), result.getDescription());
        assertEquals(testGroup.getCreatorId(), result.getCreatorId());

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(groupRepository).save(any(QuestionGroup.class));
    }

    @Test
    @DisplayName("创建题目组 - 机构不存在")
    void createGroup_InstitutionNotFound() {
        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionGroupService.createGroup(testGroupDTO, testUser.getId());
        });

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(groupRepository, never()).save(any(QuestionGroup.class));
    }

    @Test
    @DisplayName("获取题目组详情 - 成功")
    void getGroupById_Success() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testGroup));
        when(groupItemRepository.findByGroupId(anyLong())).thenReturn(List.of(testGroupItem));
        when(questionService.getQuestionsByIds(anyList())).thenReturn(List.of(testQuestionVO));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // 执行测试
        QuestionGroupVO result = questionGroupService.getGroupById(testGroup.getId(), testInstitution.getId(), true);

        // 验证结果
        assertNotNull(result);
        assertEquals(testGroup.getId(), result.getId());
        assertEquals(testGroup.getName(), result.getName());
        assertEquals(testGroup.getDescription(), result.getDescription());
        assertEquals(testGroup.getCreatorId(), result.getCreatorId());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(testGroup.getId(), testInstitution.getId());
        verify(groupItemRepository).findByGroupId(testGroup.getId());
        verify(questionService).getQuestionsByIds(anyList());
    }

    @Test
    @DisplayName("获取题目组详情 - 题目组不存在")
    void getGroupById_GroupNotFound() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionGroupService.getGroupById(999L, testInstitution.getId(), true);
        });

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(groupItemRepository, never()).findByGroupId(anyLong());
    }

    @Test
    @DisplayName("添加题目到题目组 - 成功")
    void addQuestionToGroup_Success() {
        // 设置模拟行为
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(testGroup));
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(groupItemRepository.existsByGroupIdAndQuestionId(anyLong(), anyLong())).thenReturn(false);
        when(groupItemRepository.save(any(QuestionGroupItem.class))).thenReturn(testGroupItem);
        when(questionService.getQuestionById(anyLong(), anyLong())).thenReturn(testQuestionVO);

        // 执行测试
        QuestionGroupItemVO result = questionGroupService.addQuestionToGroup(testGroupItemDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(testGroupItem.getId(), result.getId());
        assertEquals(testGroupItem.getGroup().getId(), result.getGroupId());
        assertEquals(testGroupItem.getQuestion().getId(), result.getQuestionId());
        assertEquals(testGroupItem.getOrderIndex(), result.getOrderIndex());
        assertEquals(testGroupItem.getDifficulty(), result.getDifficulty());
        assertEquals(testGroupItem.getScore(), result.getScore());

        // 验证方法调用
        verify(groupRepository).findById(testGroup.getId());
        verify(questionRepository).findById(testQuestion.getId());
        verify(groupItemRepository).existsByGroupIdAndQuestionId(testGroup.getId(), testQuestion.getId());
        verify(groupItemRepository).save(any(QuestionGroupItem.class));
        verify(questionService).getQuestionById(testQuestion.getId(), testQuestion.getInstitutionId());
    }

    @Test
    @DisplayName("添加题目到题目组 - 题目组不存在")
    void addQuestionToGroup_GroupNotFound() {
        // 设置模拟行为
        when(groupRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionGroupService.addQuestionToGroup(testGroupItemDTO);
        });

        // 验证方法调用
        verify(groupRepository).findById(testGroup.getId());
        verify(questionRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("添加题目到题目组 - 题目不存在")
    void addQuestionToGroup_QuestionNotFound() {
        // 设置模拟行为
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(testGroup));
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionGroupService.addQuestionToGroup(testGroupItemDTO);
        });

        // 验证方法调用
        verify(groupRepository).findById(testGroup.getId());
        verify(questionRepository).findById(testQuestion.getId());
        verify(groupItemRepository, never()).save(any(QuestionGroupItem.class));
    }

    @Test
    @DisplayName("添加题目到题目组 - 题目已存在")
    void addQuestionToGroup_QuestionAlreadyExists() {
        // 设置模拟行为
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(testGroup));
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(groupItemRepository.existsByGroupIdAndQuestionId(anyLong(), anyLong())).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            questionGroupService.addQuestionToGroup(testGroupItemDTO);
        });

        // 验证方法调用
        verify(groupRepository).findById(testGroup.getId());
        verify(questionRepository).findById(testQuestion.getId());
        verify(groupItemRepository).existsByGroupIdAndQuestionId(testGroup.getId(), testQuestion.getId());
        verify(groupItemRepository, never()).save(any(QuestionGroupItem.class));
    }

    @Test
    @DisplayName("从题目组移除题目 - 成功")
    void removeQuestionFromGroup_Success() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testGroup));
        when(groupItemRepository.findById(anyLong())).thenReturn(Optional.of(testGroupItem));
        doNothing().when(groupItemRepository).delete(any(QuestionGroupItem.class));

        // 执行测试
        boolean result = questionGroupService.removeQuestionFromGroup(testGroup.getId(), testGroupItem.getId(), testInstitution.getId());

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(testGroup.getId(), testInstitution.getId());
        verify(groupItemRepository).findById(testGroupItem.getId());
        verify(groupItemRepository).delete(testGroupItem);
    }

    @Test
    @DisplayName("从题目组移除题目 - 题目组不存在")
    void removeQuestionFromGroup_GroupNotFound() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionGroupService.removeQuestionFromGroup(999L, testGroupItem.getId(), testInstitution.getId());
        });

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(groupItemRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("更新题目顺序 - 成功")
    void updateItemsOrder_Success() {
        // 模拟数据
        List<QuestionGroupItemDTO> itemDTOs = new ArrayList<>();
        itemDTOs.add(testGroupItemDTO);
        
        Map<Long, QuestionGroupItem> itemMap = new HashMap<>();
        itemMap.put(testGroupItem.getId(), testGroupItem);

        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testGroup));
        when(groupItemRepository.findByGroupId(anyLong())).thenReturn(List.of(testGroupItem));
        when(groupItemRepository.save(any(QuestionGroupItem.class))).thenReturn(testGroupItem);

        // 执行测试
        boolean result = questionGroupService.updateItemsOrder(testGroup.getId(), itemDTOs, testInstitution.getId());

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(testGroup.getId(), testInstitution.getId());
        verify(groupItemRepository).findByGroupId(testGroup.getId());
        verify(groupItemRepository).save(any(QuestionGroupItem.class));
    }

    @Test
    @DisplayName("关联题目组到章节 - 成功")
    void associateGroupToSection_Success() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testGroup));
        when(sectionGroupRepository.existsByGroupIdAndSectionId(anyLong(), anyLong())).thenReturn(false);
        when(sectionGroupRepository.save(any(SectionQuestionGroup.class))).thenReturn(new SectionQuestionGroup());

        // 执行测试
        boolean result = questionGroupService.associateGroupToSection(testGroup.getId(), 1L, testInstitution.getId());

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(testGroup.getId(), testInstitution.getId());
        verify(sectionGroupRepository).existsByGroupIdAndSectionId(testGroup.getId(), 1L);
        verify(sectionGroupRepository).save(any(SectionQuestionGroup.class));
    }

    @Test
    @DisplayName("根据章节ID获取关联的题目组 - 成功")
    void getGroupsBySectionId_Success() {
        // 设置模拟行为
        when(sectionGroupRepository.findGroupIdsBySectionId(anyLong())).thenReturn(List.of(testGroup.getId()));
        when(groupRepository.findByIdInAndInstitutionId(anyList(), anyLong())).thenReturn(List.of(testGroup));
        when(groupRepository.countQuestionsByGroupIds(anyList())).thenReturn(testQuestionCountResult);

        // 执行测试
        List<QuestionGroupVO> results = questionGroupService.getGroupsBySectionId(1L, testInstitution.getId());

        // 验证结果
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testGroup.getId(), results.get(0).getId());
        assertEquals(testGroup.getName(), results.get(0).getName());
        assertEquals(testGroup.getDescription(), results.get(0).getDescription());
        assertEquals(5L, results.get(0).getQuestionCount());

        // 验证方法调用
        verify(sectionGroupRepository).findGroupIdsBySectionId(1L);
        verify(groupRepository).findByIdInAndInstitutionId(List.of(testGroup.getId()), testInstitution.getId());
        verify(groupRepository).countQuestionsByGroupIds(List.of(testGroup.getId()));
    }

    @Test
    @DisplayName("更新题目组 - 成功")
    void updateGroup_Success() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(QuestionGroup.class))).thenReturn(testGroup);

        // 修改测试数据
        QuestionGroupDTO updatedGroupDTO = QuestionGroupDTO.builder()
                .id(testGroup.getId())
                .institutionId(testInstitution.getId())
                .name("更新后的题目组")
                .description("这是更新后的题目组描述")
                .build();

        // 执行测试
        QuestionGroupVO result = questionGroupService.updateGroup(updatedGroupDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(testGroup.getId(), result.getId());
        assertEquals(updatedGroupDTO.getName(), result.getName());
        assertEquals(updatedGroupDTO.getDescription(), result.getDescription());

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(testGroup.getId(), testInstitution.getId());
        verify(groupRepository).save(any(QuestionGroup.class));
    }

    @Test
    @DisplayName("更新题目组 - 题目组不存在")
    void updateGroup_GroupNotFound() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 修改测试数据
        QuestionGroupDTO updatedGroupDTO = QuestionGroupDTO.builder()
                .id(999L)
                .institutionId(testInstitution.getId())
                .name("更新后的题目组")
                .description("这是更新后的题目组描述")
                .build();

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionGroupService.updateGroup(updatedGroupDTO);
        });

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(groupRepository, never()).save(any(QuestionGroup.class));
    }

    @Test
    @DisplayName("删除题目组 - 成功")
    void deleteGroup_Success() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testGroup));
        doNothing().when(groupItemRepository).deleteByGroupId(anyLong());
        doNothing().when(sectionGroupRepository).deleteByGroupId(anyLong());
        doNothing().when(groupRepository).delete(any(QuestionGroup.class));

        // 执行测试
        assertDoesNotThrow(() -> {
            questionGroupService.deleteGroup(testGroup.getId(), testInstitution.getId());
        });

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(testGroup.getId(), testInstitution.getId());
        verify(groupItemRepository).deleteByGroupId(testGroup.getId());
        verify(sectionGroupRepository).deleteByGroupId(testGroup.getId());
        verify(groupRepository).delete(testGroup);
    }

    @Test
    @DisplayName("删除题目组 - 题目组不存在")
    void deleteGroup_GroupNotFound() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionGroupService.deleteGroup(999L, testInstitution.getId());
        });

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(groupItemRepository, never()).deleteByGroupId(anyLong());
        verify(sectionGroupRepository, never()).deleteByGroupId(anyLong());
        verify(groupRepository, never()).delete(any(QuestionGroup.class));
    }

    @Test
    @DisplayName("获取题目组项列表 - 成功")
    void getGroupItems_Success() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testGroup));
        when(groupItemRepository.findByGroupIdOrderByOrderIndex(anyLong())).thenReturn(List.of(testGroupItem));
        when(questionService.getQuestionsByIds(anyList())).thenReturn(List.of(testQuestionVO));

        // 执行测试
        List<QuestionGroupItemVO> results = questionGroupService.getGroupItems(testGroup.getId(), testInstitution.getId());

        // 验证结果
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testGroupItem.getId(), results.get(0).getId());
        assertEquals(testGroupItem.getGroup().getId(), results.get(0).getGroupId());
        assertEquals(testGroupItem.getQuestion().getId(), results.get(0).getQuestionId());
        assertEquals(testGroupItem.getOrderIndex(), results.get(0).getOrderIndex());
        assertEquals(testGroupItem.getDifficulty(), results.get(0).getDifficulty());
        assertEquals(testGroupItem.getScore(), results.get(0).getScore());

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(testGroup.getId(), testInstitution.getId());
        verify(groupItemRepository).findByGroupIdOrderByOrderIndex(testGroup.getId());
        verify(questionService).getQuestionsByIds(anyList());
    }

    @Test
    @DisplayName("获取题目组项列表 - 题目组不存在")
    void getGroupItems_GroupNotFound() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionGroupService.getGroupItems(999L, testInstitution.getId());
        });

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(groupItemRepository, never()).findByGroupIdOrderByOrderIndex(anyLong());
    }

    @Test
    @DisplayName("取消题目组与章节的关联 - 成功")
    void dissociateGroupFromSection_Success() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testGroup));
        doNothing().when(sectionGroupRepository).deleteByGroupIdAndSectionId(anyLong(), anyLong());

        // 执行测试
        boolean result = questionGroupService.dissociateGroupFromSection(testGroup.getId(), 1L, testInstitution.getId());

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(testGroup.getId(), testInstitution.getId());
        verify(sectionGroupRepository).deleteByGroupIdAndSectionId(testGroup.getId(), 1L);
    }

    @Test
    @DisplayName("取消题目组与章节的关联 - 题目组不存在")
    void dissociateGroupFromSection_GroupNotFound() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionGroupService.dissociateGroupFromSection(999L, 1L, testInstitution.getId());
        });

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(sectionGroupRepository, never()).deleteByGroupIdAndSectionId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("取消题目组与章节的关联 - 异常情况")
    void dissociateGroupFromSection_Exception() {
        // 设置模拟行为
        when(groupRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testGroup));
        doThrow(new RuntimeException("删除失败")).when(sectionGroupRepository).deleteByGroupIdAndSectionId(anyLong(), anyLong());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            questionGroupService.dissociateGroupFromSection(testGroup.getId(), 1L, testInstitution.getId());
        });

        // 验证方法调用
        verify(groupRepository).findByIdAndInstitutionId(testGroup.getId(), testInstitution.getId());
        verify(sectionGroupRepository).deleteByGroupIdAndSectionId(testGroup.getId(), 1L);
    }
} 