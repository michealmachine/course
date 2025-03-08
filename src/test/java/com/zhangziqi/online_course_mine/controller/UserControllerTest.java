package com.zhangziqi.online_course_mine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.model.dto.UserDTO;
import com.zhangziqi.online_course_mine.model.dto.UserQueryDTO;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import com.zhangziqi.online_course_mine.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserVO userVO;
    private UserDTO userDTO;
    private List<UserVO> userVOList;

    @BeforeEach
    public void setup() {
        // 模拟角色
        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder()
                .id(1L)
                .name("普通用户")
                .code("ROLE_USER")
                .permissions(new HashSet<>())
                .build());

        // 模拟用户VO
        userVO = UserVO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phone("13800138000")
                .avatar("avatar.jpg")
                .nickname("测试用户")
                .status(1)
                .institutionId(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .roles(roles)
                .build();

        // 模拟用户列表
        userVOList = new ArrayList<>();
        userVOList.add(userVO);
        userVOList.add(UserVO.builder()
                .id(2L)
                .username("testuser2")
                .email("test2@example.com")
                .phone("13800138001")
                .avatar("avatar2.jpg")
                .nickname("测试用户2")
                .status(1)
                .institutionId(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .roles(roles)
                .build());

        // 模拟用户DTO
        userDTO = UserDTO.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .phone("13800138000")
                .avatar("avatar.jpg")
                .nickname("测试用户")
                .status(1)
                .roleIds(Set.of(1L))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserList() throws Exception {
        // 构建分页结果
        Page<UserVO> page = new PageImpl<>(userVOList);
        when(userService.getUserList(any(UserQueryDTO.class))).thenReturn(page);

        // 执行测试
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].username", is("testuser")))
                .andExpect(jsonPath("$.data.content[1].username", is("testuser2")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userVO);

        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.username", is("testuser")))
                .andExpect(jsonPath("$.data.email", is("test@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateUser() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenReturn(userVO);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.username", is("testuser")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateUser() throws Exception {
        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(userVO);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.username", is("testuser")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateUserStatus() throws Exception {
        UserVO updatedUserVO = UserVO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status(0) // 禁用状态
                .build();
        
        when(userService.updateUserStatus(eq(1L), eq(0))).thenReturn(updatedUserVO);

        mockMvc.perform(patch("/api/users/1/status")
                .param("status", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.status", is(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAssignRoles() throws Exception {
        Set<Long> roleIds = Set.of(1L, 2L);
        
        when(userService.assignRoles(eq(1L), eq(roleIds))).thenReturn(userVO);

        mockMvc.perform(put("/api/users/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testBatchDeleteUsers() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        
        doNothing().when(userService).batchDeleteUsers(ids);

        mockMvc.perform(delete("/api/users/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isNoContent());
    }
} 