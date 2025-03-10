package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.RoleDTO;
import com.zhangziqi.online_course_mine.model.entity.Permission;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.vo.RoleVO;
import com.zhangziqi.online_course_mine.repository.PermissionRepository;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;
    private RoleDTO testRoleDTO;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        // 创建测试权限
        testPermission = Permission.builder()
                .id(1L)
                .name("测试权限")
                .code("TEST_READ")
                .url("/api/test/**")
                .method("GET")
                .description("测试权限描述")
                .build();

        // 创建测试角色
        testRole = Role.builder()
                .id(1L)
                .name("测试角色")
                .code("ROLE_TEST")
                .description("测试角色描述")
                .permissions(new HashSet<>(Collections.singletonList(testPermission)))
                .build();

        // 创建测试角色DTO
        testRoleDTO = new RoleDTO();
        testRoleDTO.setName("测试角色");
        testRoleDTO.setCode("ROLE_TEST");
        testRoleDTO.setDescription("测试角色描述");
        testRoleDTO.setPermissionIds(new HashSet<>(Collections.singletonList(1L)));
    }

    @Test
    void getRoleListShouldReturnAllRoles() {
        // 准备测试数据
        List<Role> roles = Arrays.asList(testRole);
        
        // 模拟Repository方法
        when(roleRepository.findAll()).thenReturn(roles);

        // 执行测试
        List<RoleVO> result = roleService.getRoleList();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRole.getId(), result.get(0).getId());
        assertEquals(testRole.getName(), result.get(0).getName());
        assertEquals(testRole.getCode(), result.get(0).getCode());
    }

    @Test
    void getRoleByIdShouldReturnRoleWhenExists() {
        // 模拟Repository方法
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));

        // 执行测试
        RoleVO result = roleService.getRoleById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testRole.getId(), result.getId());
        assertEquals(testRole.getName(), result.getName());
    }

    @Test
    void getRoleByIdShouldThrowExceptionWhenNotExists() {
        // 模拟Repository方法
        when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.getRoleById(1L));
    }

    @Test
    void getRoleByCodeShouldReturnRoleWhenExists() {
        // 模拟Repository方法
        when(roleRepository.findByCode(anyString())).thenReturn(Optional.of(testRole));

        // 执行测试
        Role result = roleService.getRoleByCode("ROLE_TEST");

        // 验证结果
        assertNotNull(result);
        assertEquals(testRole.getId(), result.getId());
        assertEquals(testRole.getCode(), result.getCode());
    }

    @Test
    void getRoleByCodeShouldThrowExceptionWhenNotExists() {
        // 模拟Repository方法
        when(roleRepository.findByCode(anyString())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.getRoleByCode("ROLE_NOT_EXISTS"));
    }

    @Test
    void createRoleShouldReturnCreatedRole() {
        // 模拟Repository方法
        when(roleRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // 执行测试
        RoleVO result = roleService.createRole(testRoleDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(testRole.getId(), result.getId());
        assertEquals(testRole.getName(), result.getName());
        assertEquals(testRole.getCode(), result.getCode());
        
        // 验证仓库方法被调用
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void createRoleShouldThrowExceptionWhenCodeExists() {
        // 模拟Repository方法
        when(roleRepository.findByCode(anyString())).thenReturn(Optional.of(testRole));

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.createRole(testRoleDTO));
    }

    @Test
    void createRoleShouldThrowExceptionWhenNameExists() {
        // 模拟Repository方法
        when(roleRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(testRole));

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.createRole(testRoleDTO));
    }

    @Test
    void updateRoleShouldReturnUpdatedRole() {
        // 准备更新数据
        RoleDTO updateDTO = new RoleDTO();
        updateDTO.setName("更新角色");
        updateDTO.setCode("ROLE_UPDATE");
        updateDTO.setDescription("更新角色描述");
        Set<Long> permissionIds = new HashSet<>(Collections.singletonList(1L));
        updateDTO.setPermissionIds(permissionIds);

        // 准备更新后的角色
        Role updatedRole = Role.builder()
                .id(1L)
                .name("更新角色")
                .code("ROLE_UPDATE")
                .description("更新角色描述")
                .permissions(new HashSet<>(Collections.singletonList(testPermission)))
                .build();

        // 模拟Repository方法
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));
        when(roleRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);

        // 执行测试
        RoleVO result = roleService.updateRole(1L, updateDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(updatedRole.getId(), result.getId());
        assertEquals(updatedRole.getName(), result.getName());
        assertEquals(updatedRole.getCode(), result.getCode());
        
        // 验证仓库方法被调用
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void updateRoleShouldThrowExceptionWhenRoleNotExists() {
        // 模拟Repository方法
        when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.updateRole(1L, testRoleDTO));
    }

    @Test
    void updateRoleShouldThrowExceptionWhenCodeExists() {
        // 创建另一个角色
        Role anotherRole = Role.builder()
                .id(2L)
                .name("另一个角色")
                .code("ROLE_ANOTHER")
                .build();

        // 模拟Repository方法
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));
        when(roleRepository.findByCode("ROLE_ANOTHER")).thenReturn(Optional.of(anotherRole));

        // 准备更新数据
        RoleDTO updateDTO = new RoleDTO();
        updateDTO.setCode("ROLE_ANOTHER");

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.updateRole(1L, updateDTO));
    }

    @Test
    void deleteRoleShouldSucceedWhenRoleExists() {
        // 模拟Repository方法
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(roleRepository).delete(any(Role.class));

        // 执行测试
        roleService.deleteRole(1L);

        // 验证方法被调用
        verify(roleRepository, times(1)).delete(any(Role.class));
    }

    @Test
    void deleteRoleShouldThrowExceptionWhenRoleNotExists() {
        // 模拟Repository方法
        when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.deleteRole(1L));
    }

    @Test
    void deleteRoleShouldThrowExceptionWhenRoleIsReferenced() {
        // 模拟Repository方法 - 角色被用户引用
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));
        when(userRepository.findAll()).thenReturn(Collections.singletonList(mock(com.zhangziqi.online_course_mine.model.entity.User.class)));

        // 设置mock用户引用了角色
        when(userRepository.findAll().get(0).getRoles()).thenReturn(Set.of(testRole));

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.deleteRole(1L));
    }

    @Test
    void assignPermissionsShouldReturnUpdatedRole() {
        // 准备权限ID
        Set<Long> permissionIds = new HashSet<>(Collections.singletonList(1L));

        // 模拟Repository方法
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // 执行测试
        RoleVO result = roleService.assignPermissions(1L, permissionIds);

        // 验证结果
        assertNotNull(result);
        assertEquals(testRole.getId(), result.getId());
        
        // 验证仓库方法被调用
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void assignPermissionsShouldThrowExceptionWhenRoleNotExists() {
        // 模拟Repository方法
        when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.assignPermissions(1L, Set.of(1L)));
    }

    @Test
    void assignPermissionsShouldThrowExceptionWhenPermissionIdsIsEmpty() {
        // 模拟Repository方法
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.assignPermissions(1L, Collections.emptySet()));
    }

    @Test
    void batchDeleteRolesShouldSucceedWhenRolesExist() {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L);
        Role role1 = Role.builder().id(1L).name("角色1").code("ROLE_1").build();
        Role role2 = Role.builder().id(2L).name("角色2").code("ROLE_2").build();
        
        // 模拟Repository方法
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role1));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role2));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(roleRepository).delete(any(Role.class));

        // 执行测试
        roleService.batchDeleteRoles(ids);

        // 验证方法被调用
        verify(roleRepository, times(2)).delete(any(Role.class));
    }

    @Test
    void batchDeleteRolesShouldThrowExceptionWhenIdsIsEmpty() {
        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> roleService.batchDeleteRoles(Collections.emptyList()));
    }
} 