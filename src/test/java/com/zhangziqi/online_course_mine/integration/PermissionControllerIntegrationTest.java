package com.zhangziqi.online_course_mine.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.config.TestSecurityConfig;
import com.zhangziqi.online_course_mine.model.dto.PermissionDTO;
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
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class PermissionControllerIntegrationTest {

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

    private final String BASE_URL = "/api/permissions";

    @BeforeEach
    void setUp() {
        // 清理数据
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // 创建测试权限 - 使用与TestUserDetailsService一致的权限代码
        testPermission = permissionRepository.save(Permission.builder()
                .name("测试权限")
                .code("TEST_READ")  // 与TestUserDetailsService中的权限代码一致
                .url("/api/test/**")
                .method("GET")
                .description("测试权限描述")
                .build());

        // 创建角色 - 使用与TestUserDetailsService一致的角色代码
        adminRole = roleRepository.save(Role.builder()
                .name("管理员")
                .code("ROLE_ADMIN") // 与TestUserDetailsService中的角色代码一致
                .description("管理员角色")
                .build());

        userRole = roleRepository.save(Role.builder()
                .name("普通用户")
                .code("ROLE_USER") // 与TestUserDetailsService中的角色代码一致
                .description("普通用户角色")
                .build());

        // 给角色分配权限
        adminRole.setPermissions(Set.of(testPermission));
        roleRepository.save(adminRole);

        // 创建测试用户 - 用户名与TestUserDetailsService中的一致
        adminUser = User.builder()
                .username("admin_test") // 与TestUserDetailsService中的用户名一致
                .password(passwordEncoder.encode("password"))
                .email("admin@test.com")
                .status(1)
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(adminUser);

        normalUser = User.builder()
                .username("user_test") // 与TestUserDetailsService中的用户名一致
                .password(passwordEncoder.encode("password"))
                .email("user@test.com")
                .status(1)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(normalUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    @Test
    @WithUserDetails(value = "admin_test", userDetailsServiceBeanName = "userDetailsService")
    void getPermissionListShouldReturnPermissionListWhenUserIsAdmin() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(testPermission.getId()));
    }

    @Test
    @WithUserDetails(value = "user_test", userDetailsServiceBeanName = "userDetailsService")
    void getPermissionListShouldReturnForbiddenWhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "admin_test", userDetailsServiceBeanName = "userDetailsService")
    void createPermissionShouldSucceedWhenUserIsAdmin() throws Exception {
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("新权限");
        permissionDTO.setCode("NEW_CREATE");  // 修改为大写字母、数字、下划线格式
        permissionDTO.setUrl("/api/new");
        permissionDTO.setMethod("POST");
        permissionDTO.setDescription("新权限描述");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("新权限"));
    }

    @Test
    @WithUserDetails(value = "admin_test", userDetailsServiceBeanName = "userDetailsService")
    void updatePermissionShouldSucceedWhenUserIsAdmin() throws Exception {
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("更新权限");
        permissionDTO.setCode("TEST_UPDATE");  // 修改为大写字母、数字、下划线格式
        permissionDTO.setUrl("/api/test/**");
        permissionDTO.setMethod("PUT");
        permissionDTO.setDescription("更新权限描述");

        mockMvc.perform(put(BASE_URL + "/" + testPermission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("更新权限"));
    }

    @Test
    @WithUserDetails(value = "admin_test", userDetailsServiceBeanName = "userDetailsService")
    void deletePermissionShouldSucceedWhenUserIsAdmin() throws Exception {
        // 在删除权限前先解除权限与角色的关联
        adminRole.setPermissions(Collections.emptySet());
        roleRepository.save(adminRole);

        // 然后再测试删除权限
        mockMvc.perform(delete(BASE_URL + "/" + testPermission.getId()))
                .andExpect(status().isNoContent());
    }
} 