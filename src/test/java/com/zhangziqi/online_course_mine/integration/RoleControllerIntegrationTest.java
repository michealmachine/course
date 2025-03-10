package com.zhangziqi.online_course_mine.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.config.TestSecurityConfig;
import com.zhangziqi.online_course_mine.model.dto.RoleDTO;
import com.zhangziqi.online_course_mine.model.entity.Permission;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.repository.PermissionRepository;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Role adminRole;
    private Role userRole;
    private User adminUser;
    private User normalUser;
    private Permission testPermission;
    private Role testRole;

    private final String BASE_URL = "/api/roles";

    @BeforeEach
    void setUp() {
        // 清理数据
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // 创建测试权限
        testPermission = Permission.builder()
                .name("测试权限")
                .code("TEST_READ")
                .url("/api/test/**")
                .method("GET")
                .description("测试权限描述")
                .build();
        testPermission = permissionRepository.save(testPermission);

        // 创建管理员角色
        adminRole = Role.builder()
                .name("管理员")
                .code("ROLE_ADMIN")
                .description("管理员角色")
                .permissions(new HashSet<>(Collections.singletonList(testPermission)))
                .build();
        adminRole = roleRepository.save(adminRole);

        // 创建普通用户角色
        userRole = Role.builder()
                .name("普通用户")
                .code("ROLE_USER")
                .description("普通用户角色")
                .build();
        userRole = roleRepository.save(userRole);

        // 创建测试角色
        testRole = Role.builder()
                .name("测试角色")
                .code("ROLE_TEST")
                .description("测试角色描述")
                .build();
        testRole = roleRepository.save(testRole);

        // 创建管理员用户
        adminUser = User.builder()
                .username("admin_test")
                .password(passwordEncoder.encode("password"))
                .email("admin@test.com")
                .status(1)
                .roles(Set.of(adminRole))
                .build();
        adminUser = userRepository.save(adminUser);

        // 创建普通用户
        normalUser = User.builder()
                .username("user_test")
                .password(passwordEncoder.encode("password"))
                .email("user@test.com")
                .status(1)
                .roles(Set.of(userRole))
                .build();
        normalUser = userRepository.save(normalUser);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    @Test
    @WithUserDetails("admin_test")
    void getRoleListShouldReturnRoleListWhenUserIsAdmin() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3)); // 包含3个角色
    }

    @Test
    @WithUserDetails("user_test")
    void getRoleListShouldReturnForbiddenWhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin_test")
    void getRoleByIdShouldReturnRoleWhenUserIsAdmin() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testRole.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试角色"))
                .andExpect(jsonPath("$.data.code").value("ROLE_TEST"));
    }

    @Test
    @WithUserDetails("admin_test")
    void createRoleShouldSucceedWhenUserIsAdmin() throws Exception {
        // 准备创建角色的数据
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("新角色");
        roleDTO.setCode("ROLE_NEW");
        roleDTO.setDescription("新角色描述");

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("新角色"))
                .andExpect(jsonPath("$.data.code").value("ROLE_NEW"));
    }

    @Test
    @WithUserDetails("admin_test")
    void updateRoleShouldSucceedWhenUserIsAdmin() throws Exception {
        // 准备更新角色的数据
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("更新的测试角色");
        roleDTO.setCode("ROLE_TEST");
        roleDTO.setDescription("更新的测试角色描述");

        mockMvc.perform(put(BASE_URL + "/" + testRole.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("更新的测试角色"))
                .andExpect(jsonPath("$.data.description").value("更新的测试角色描述"));
    }

    @Test
    @WithUserDetails("admin_test")
    void deleteRoleShouldSucceedWhenUserIsAdmin() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testRole.getId()))
                .andExpect(status().isNoContent());

        // 确认角色已被删除
        mockMvc.perform(get(BASE_URL + "/" + testRole.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @WithUserDetails("admin_test")
    void assignPermissionsShouldSucceedWhenUserIsAdmin() throws Exception {
        // 准备权限ID列表
        List<Long> permissionIds = Collections.singletonList(testPermission.getId());

        mockMvc.perform(put(BASE_URL + "/" + testRole.getId() + "/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.permissions[0].id").value(testPermission.getId()));
    }

    @Test
    @WithUserDetails("admin_test")
    void batchDeleteRolesShouldSucceedWhenUserIsAdmin() throws Exception {
        // 准备角色ID列表
        List<Long> roleIds = Collections.singletonList(testRole.getId());

        mockMvc.perform(delete(BASE_URL + "/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isNoContent());

        // 确认角色已被删除
        mockMvc.perform(get(BASE_URL + "/" + testRole.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
} 