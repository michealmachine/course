package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.QuestionGroupDTO;
import com.zhangziqi.online_course_mine.model.dto.QuestionGroupItemDTO;
import com.zhangziqi.online_course_mine.model.vo.QuestionGroupItemVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionGroupVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.QuestionGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目组控制器
 * 处理题目组的创建、更新、查询和管理请求
 */
@Slf4j
@RestController
@RequestMapping("/api/questions/groups")
@RequiredArgsConstructor
@Tag(name = "题目组管理", description = "题目组的创建、更新、查询和管理相关操作")
public class QuestionGroupController {

    private final QuestionGroupService groupService;

    /**
     * 创建题目组
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "创建题目组", description = "创建一个新的题目组")
    public Result<QuestionGroupVO> createGroup(@Valid @RequestBody QuestionGroupDTO groupDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("创建题目组, 用户ID: {}, 机构ID: {}, 题目组名称: {}", 
                userId, institutionId, groupDTO.getName());
        
        // 设置机构ID
        groupDTO.setInstitutionId(institutionId);
        
        QuestionGroupVO groupVO = groupService.createGroup(groupDTO, userId);
        return Result.success(groupVO);
    }

    /**
     * 更新题目组
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新题目组", description = "更新指定ID的题目组信息")
    public Result<QuestionGroupVO> updateGroup(
            @Parameter(description = "题目组ID") @PathVariable("id") Long id,
            @Valid @RequestBody QuestionGroupDTO groupDTO) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新题目组, 题目组ID: {}, 机构ID: {}, 题目组名称: {}", 
                id, institutionId, groupDTO.getName());
        
        // 设置题目组ID和机构ID
        groupDTO.setId(id);
        groupDTO.setInstitutionId(institutionId);
        
        QuestionGroupVO groupVO = groupService.updateGroup(groupDTO);
        return Result.success(groupVO);
    }

    /**
     * 获取题目组详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取题目组详情", description = "获取指定ID的题目组详细信息")
    public Result<QuestionGroupVO> getGroup(
            @Parameter(description = "题目组ID") @PathVariable("id") Long id,
            @Parameter(description = "是否包含题目项") @RequestParam(required = false, defaultValue = "true") boolean includeItems) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取题目组详情, 题目组ID: {}, 机构ID: {}, 包含题目项: {}", 
                id, institutionId, includeItems);
        
        QuestionGroupVO groupVO = groupService.getGroupById(id, institutionId, includeItems);
        return Result.success(groupVO);
    }

    /**
     * 删除题目组
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "删除题目组", description = "删除指定ID的题目组")
    public Result<Void> deleteGroup(
            @Parameter(description = "题目组ID") @PathVariable("id") Long id) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("删除题目组, 题目组ID: {}, 机构ID: {}", id, institutionId);
        
        groupService.deleteGroup(id, institutionId);
        return Result.success();
    }

    /**
     * 分页查询题目组列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取题目组列表", description = "分页获取机构的题目组列表")
    public Result<Page<QuestionGroupVO>> getGroups(
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取题目组列表, 机构ID: {}, 关键词: {}, 页码: {}, 每页条数: {}", 
                institutionId, keyword, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionGroupVO> groupPage = groupService.getGroups(institutionId, keyword, pageable);
        return Result.success(groupPage);
    }

    /**
     * 获取所有题目组
     */
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取所有题目组", description = "获取机构的所有题目组")
    public Result<List<QuestionGroupVO>> getAllGroups() {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取所有题目组, 机构ID: {}", institutionId);
        
        List<QuestionGroupVO> groups = groupService.getAllGroups(institutionId);
        return Result.success(groups);
    }

    /**
     * 为题目组添加题目
     */
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "添加题目到题目组", description = "向题目组中添加题目")
    public Result<QuestionGroupItemVO> addQuestionToGroup(@Valid @RequestBody QuestionGroupItemDTO itemDTO) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("添加题目到题目组, 题目组ID: {}, 题目ID: {}, 机构ID: {}", 
                itemDTO.getGroupId(), itemDTO.getQuestionId(), institutionId);
        
        QuestionGroupItemVO itemVO = groupService.addQuestionToGroup(itemDTO);
        return Result.success(itemVO);
    }

    /**
     * 更新题目组项
     */
    @PutMapping("/items/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新题目组项", description = "更新题目组中的题目项信息")
    public Result<QuestionGroupItemVO> updateGroupItem(
            @Parameter(description = "题目组项ID") @PathVariable("id") Long id,
            @Valid @RequestBody QuestionGroupItemDTO itemDTO) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新题目组项, 题目组项ID: {}, 题目组ID: {}, 机构ID: {}", 
                id, itemDTO.getGroupId(), institutionId);
        
        itemDTO.setId(id);
        QuestionGroupItemVO itemVO = groupService.updateGroupItem(itemDTO);
        return Result.success(itemVO);
    }

    /**
     * 从题目组移除题目
     */
    @DeleteMapping("/{groupId}/items/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "从题目组移除题目", description = "从题目组中移除指定的题目项")
    public Result<Void> removeQuestionFromGroup(
            @Parameter(description = "题目组ID") @PathVariable("groupId") Long groupId,
            @Parameter(description = "题目组项ID") @PathVariable("itemId") Long itemId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("从题目组移除题目, 题目组ID: {}, 题目组项ID: {}, 机构ID: {}", 
                groupId, itemId, institutionId);
        
        groupService.removeQuestionFromGroup(groupId, itemId, institutionId);
        return Result.success();
    }

    /**
     * 获取题目组中的所有题目
     */
    @GetMapping("/{groupId}/items")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取题目组中的所有题目", description = "获取指定题目组中的所有题目项")
    public Result<List<QuestionGroupItemVO>> getGroupItems(
            @Parameter(description = "题目组ID") @PathVariable("groupId") Long groupId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取题目组中的所有题目, 题目组ID: {}, 机构ID: {}", groupId, institutionId);
        
        List<QuestionGroupItemVO> items = groupService.getGroupItems(groupId, institutionId);
        return Result.success(items);
    }

    /**
     * 更新题目组中题目的顺序
     */
    @PutMapping("/{groupId}/items/order")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新题目顺序", description = "更新题目组中题目的顺序")
    public Result<Boolean> updateItemsOrder(
            @Parameter(description = "题目组ID") @PathVariable("groupId") Long groupId,
            @RequestBody List<QuestionGroupItemDTO> itemDTOs) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新题目顺序, 题目组ID: {}, 机构ID: {}, 题目项数量: {}", 
                groupId, institutionId, itemDTOs.size());
        
        boolean result = groupService.updateItemsOrder(groupId, itemDTOs, institutionId);
        return Result.success(result);
    }

    /**
     * 批量添加题目到题目组
     */
    @PostMapping("/{groupId}/questions")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "批量添加题目到题目组", description = "向题目组中批量添加多个题目")
    public Result<List<QuestionGroupItemVO>> addQuestionsToGroup(
            @Parameter(description = "题目组ID") @PathVariable("groupId") Long groupId,
            @RequestBody List<Long> questionIds) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("批量添加题目到题目组, 题目组ID: {}, 题目数量: {}, 机构ID: {}", 
                groupId, questionIds.size(), institutionId);
        
        List<QuestionGroupItemVO> items = groupService.addQuestionsToGroup(groupId, questionIds, institutionId);
        return Result.success(items);
    }
} 