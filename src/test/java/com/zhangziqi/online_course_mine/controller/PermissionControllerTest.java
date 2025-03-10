package com.zhangziqi.online_course_mine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.model.dto.PermissionDTO;
import com.zhangziqi.online_course_mine.model.vo.PermissionVO;
import com.zhangziqi.online_course_mine.service.PermissionService;
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

import java.util.Arrays;
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
public class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PermissionService permissionService;

    private final String BASE_URL = "/api/permissions";
    
    private PermissionVO testPermission;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testPermission = PermissionVO.builder()
                .id(1L)
                .name("测试权限")
                .code("TEST_READ")
                .url("/api/test/**")
                .method("GET")
                .description("测试权限描述")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN") // 使用ADMIN角色进行测试
    void getPermissionListShouldReturnPermissionListWhenUserHasAdminRole() throws Exception {
        // 准备测试数据
        List<PermissionVO> permissions = Arrays.asList(testPermission);
        
        // 模拟服务方法
        when(permissionService.getPermissionList()).thenReturn(permissions);

        // 执行测试
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试权限"));
    }

    @Test
    @WithMockUser(roles = "USER") // 使用普通用户角色进行测试
    void getPermissionListShouldReturnForbiddenWhenUserDoesNotHaveAdminRole() throws Exception {
        // 执行测试，期望返回403
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPermissionByIdShouldReturnPermissionWhenUserHasAdminRole() throws Exception {
        // 模拟服务方法
        when(permissionService.getPermissionById(anyLong())).thenReturn(testPermission);

        // 执行测试
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试权限"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPermissionShouldReturnCreatedPermissionWhenUserHasAdminRole() throws Exception {
        // 准备请求数据
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("新权限");
        permissionDTO.setCode("NEW_CREATE");
        permissionDTO.setUrl("/api/new");
        permissionDTO.setMethod("POST");
        permissionDTO.setDescription("新权限描述");

        // 模拟服务方法
        when(permissionService.createPermission(any(PermissionDTO.class))).thenReturn(testPermission);

        // 执行测试
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePermissionShouldReturnUpdatedPermissionWhenUserHasAdminRole() throws Exception {
        // 准备请求数据
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("更新权限");
        permissionDTO.setCode("TEST_UPDATE");
        permissionDTO.setUrl("/api/test/**");
        permissionDTO.setMethod("PUT");
        permissionDTO.setDescription("更新权限描述");

        // 模拟服务方法
        when(permissionService.updatePermission(anyLong(), any(PermissionDTO.class))).thenReturn(testPermission);

        // 执行测试
        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePermissionShouldReturnNoContentWhenUserHasAdminRole() throws Exception {
        // 模拟服务方法
        doNothing().when(permissionService).deletePermission(anyLong());

        // 执行测试
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        // 验证服务方法被调用
        verify(permissionService, times(1)).deletePermission(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void batchDeletePermissionsShouldReturnNoContentWhenUserHasAdminRole() throws Exception {
        // 准备请求数据
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        // 模拟服务方法
        doNothing().when(permissionService).batchDeletePermissions(anyList());

        // 执行测试
        mockMvc.perform(delete(BASE_URL + "/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isNoContent());

        // 验证服务方法被调用
        verify(permissionService, times(1)).batchDeletePermissions(ids);
    }
} 