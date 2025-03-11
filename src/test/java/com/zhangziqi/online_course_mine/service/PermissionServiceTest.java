package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.PermissionDTO;
import com.zhangziqi.online_course_mine.model.entity.Permission;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.vo.PermissionVO;
import com.zhangziqi.online_course_mine.repository.PermissionRepository;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Permission testPermission;
    private PermissionDTO testPermissionDTO;

    @BeforeEach
    void setUp() {
        testPermission = Permission.builder()
                .id(1L)
                .name("测试权限")
                .code("test:read")
                .url("/api/test/**")
                .method("GET")
                .description("测试权限描述")
                .build();

        testPermissionDTO = new PermissionDTO();
        testPermissionDTO.setName("测试权限");
        testPermissionDTO.setCode("test:read");
        testPermissionDTO.setUrl("/api/test/**");
        testPermissionDTO.setMethod("GET");
        testPermissionDTO.setDescription("测试权限描述");
    }

    @Test
    void getPermissionListShouldReturnAllPermissions() {
        // 准备测试数据
        List<Permission> permissions = Arrays.asList(testPermission);
        
        // 模拟Repository方法
        when(permissionRepository.findAll()).thenReturn(permissions);

        // 执行测试
        List<PermissionVO> result = permissionService.getPermissionList();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPermission.getId(), result.get(0).getId());
        assertEquals(testPermission.getName(), result.get(0).getName());
        assertEquals(testPermission.getCode(), result.get(0).getCode());
    }

    @Test
    void getPermissionByIdShouldReturnPermissionWhenExists() {
        // 模拟Repository方法
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.of(testPermission));

        // 执行测试
        PermissionVO result = permissionService.getPermissionById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testPermission.getId(), result.getId());
        assertEquals(testPermission.getName(), result.getName());
    }

    @Test
    void getPermissionByIdShouldThrowExceptionWhenNotExists() {
        // 模拟Repository方法
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> permissionService.getPermissionById(1L));
    }

    @Test
    void createPermissionShouldReturnCreatedPermission() {
        // 模拟Repository方法
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);
        when(permissionRepository.findByCode(anyString())).thenReturn(Optional.empty());

        // 执行测试
        PermissionVO result = permissionService.createPermission(testPermissionDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(testPermission.getId(), result.getId());
        assertEquals(testPermission.getName(), result.getName());
        assertEquals(testPermission.getCode(), result.getCode());
        
        // 验证仓库方法被调用
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    void updatePermissionShouldReturnUpdatedPermission() {
        // 准备更新数据
        PermissionDTO updateDTO = new PermissionDTO();
        updateDTO.setName("更新权限");
        updateDTO.setCode("test:update");
        updateDTO.setUrl("/api/test/update");
        updateDTO.setMethod("PUT");
        updateDTO.setDescription("更新权限描述");

        // 准备更新后的权限
        Permission updatedPermission = Permission.builder()
                .id(1L)
                .name("更新权限")
                .code("test:update")
                .url("/api/test/update")
                .method("PUT")
                .description("更新权限描述")
                .build();

        // 模拟Repository方法 - 只模拟真正需要的方法
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.of(testPermission));
        when(permissionRepository.save(any(Permission.class))).thenReturn(updatedPermission);

        // 执行测试
        PermissionVO result = permissionService.updatePermission(1L, updateDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(updatedPermission.getId(), result.getId());
        assertEquals(updatedPermission.getName(), result.getName());
        assertEquals(updatedPermission.getCode(), result.getCode());
        
        // 验证仓库方法被调用
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    void deletePermissionShouldSucceedWhenPermissionExists() {
        // 准备测试数据
        // 模拟Repository方法 - 只模拟真正需要的方法
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.of(testPermission));
        doNothing().when(permissionRepository).delete(any(Permission.class));

        // 执行测试
        permissionService.deletePermission(1L);

        // 验证方法被调用
        verify(permissionRepository, times(1)).delete(any(Permission.class));
    }

    @Test
    void deletePermissionShouldThrowExceptionWhenPermissionNotExists() {
        // 模拟Repository方法
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> permissionService.deletePermission(1L));
    }


} 