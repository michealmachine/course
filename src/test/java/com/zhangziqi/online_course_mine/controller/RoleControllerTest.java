package com.zhangziqi.online_course_mine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.model.dto.RoleDTO;
import com.zhangziqi.online_course_mine.model.entity.Permission;
import com.zhangziqi.online_course_mine.model.vo.RoleVO;
import com.zhangziqi.online_course_mine.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // 不启用过滤器以便只测试控制器逻辑
@ActiveProfiles("test")
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    private final String BASE_URL = "/api/roles";
    
    private RoleVO testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testPermission = Permission.builder()
                .id(1L)
                .name("测试权限")
                .code("TEST_READ")
                .url("/api/test/**")
                .method("GET")
                .description("测试权限描述")
                .build();

        testRole = RoleVO.builder()
                .id(1L)
                .name("测试角色")
                .code("ROLE_TEST")
                .description("测试角色描述")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .permissions(new HashSet<>(Collections.singletonList(testPermission)))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN") // 使用ADMIN角色进行测试
    void getRoleListShouldReturnRoleListWhenUserHasAdminRole() throws Exception {
        // 准备测试数据
        List<RoleVO> roles = Arrays.asList(testRole);
        
        // 模拟服务方法
        when(roleService.getRoleList()).thenReturn(roles);

        // 执行测试
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试角色"));
    }

    @Test
    @WithMockUser(roles = "USER") // 使用普通用户角色进行测试
    void getRoleListShouldReturnForbiddenWhenUserDoesNotHaveAdminRole() throws Exception {
        // 执行测试，期望返回403
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoleByIdShouldReturnRoleWhenUserHasAdminRole() throws Exception {
        // 模拟服务方法
        when(roleService.getRoleById(anyLong())).thenReturn(testRole);

        // 执行测试
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试角色"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoleShouldReturnCreatedRoleWhenUserHasAdminRole() throws Exception {
        // 准备请求数据
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("新角色");
        roleDTO.setCode("ROLE_NEW");
        roleDTO.setDescription("新角色描述");

        // 模拟服务方法
        when(roleService.createRole(any(RoleDTO.class))).thenReturn(testRole);

        // 执行测试
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoleShouldReturnUpdatedRoleWhenUserHasAdminRole() throws Exception {
        // 准备请求数据
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("更新角色");
        roleDTO.setCode("ROLE_UPDATE");
        roleDTO.setDescription("更新角色描述");

        // 模拟服务方法
        when(roleService.updateRole(anyLong(), any(RoleDTO.class))).thenReturn(testRole);

        // 执行测试
        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRoleShouldReturnNoContentWhenUserHasAdminRole() throws Exception {
        // 模拟服务方法
        doNothing().when(roleService).deleteRole(anyLong());

        // 执行测试
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        // 验证服务方法被调用
        verify(roleService, times(1)).deleteRole(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignPermissionsShouldReturnUpdatedRoleWhenUserHasAdminRole() throws Exception {
        // 准备权限ID列表
        List<Long> permissionIds = Collections.singletonList(1L);
        
        // 模拟服务方法
        when(roleService.assignPermissions(anyLong(), any())).thenReturn(testRole);

        // 执行测试
        mockMvc.perform(put(BASE_URL + "/1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void batchDeleteRolesShouldReturnNoContentWhenUserHasAdminRole() throws Exception {
        // 准备角色ID列表
        List<Long> roleIds = Arrays.asList(1L, 2L);
        
        // 模拟服务方法
        doNothing().when(roleService).batchDeleteRoles(any());

        // 执行测试
        mockMvc.perform(delete(BASE_URL + "/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isNoContent());

        // 验证服务方法被调用
        verify(roleService, times(1)).batchDeleteRoles(any());
    }
} 