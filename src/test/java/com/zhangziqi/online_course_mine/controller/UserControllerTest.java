package com.zhangziqi.online_course_mine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.model.dto.ChangePasswordDTO;
import com.zhangziqi.online_course_mine.model.dto.EmailUpdateDTO;
import com.zhangziqi.online_course_mine.model.dto.UserDTO;
import com.zhangziqi.online_course_mine.model.dto.UserProfileDTO;
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
import org.springframework.mock.web.MockMultipartFile;
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
    private UserProfileDTO profileDTO;
    private ChangePasswordDTO changePasswordDTO;
    private EmailUpdateDTO emailUpdateDTO;

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

        // 模拟个人信息DTO
        profileDTO = new UserProfileDTO();
        profileDTO.setNickname("新昵称");
        profileDTO.setPhone("13900001111");
        
        // 模拟密码修改DTO
        changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword("oldPassword");
        changePasswordDTO.setNewPassword("newPassword123");
        changePasswordDTO.setConfirmPassword("newPassword123");
        
        // 模拟邮箱更新DTO
        emailUpdateDTO = new EmailUpdateDTO();
        emailUpdateDTO.setNewEmail("newemail@example.com");
        emailUpdateDTO.setEmailCode("123456");
        emailUpdateDTO.setPassword("password123");
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

    @Test
    @WithMockUser(username = "testuser")
    public void testGetCurrentUser() throws Exception {
        when(userService.getCurrentUser("testuser")).thenReturn(userVO);
        
        mockMvc.perform(get("/api/users/current")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.username", is("testuser")))
                .andExpect(jsonPath("$.data.email", is("test@example.com")))
                .andExpect(jsonPath("$.data.nickname", is("测试用户")));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateCurrentUser() throws Exception {
        when(userService.updateCurrentUserProfile(eq("testuser"), anyString(), anyString())).thenReturn(userVO);
        
        mockMvc.perform(put("/api/users/current")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.username", is("testuser")));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    public void testChangePassword() throws Exception {
        when(userService.changePassword(eq("testuser"), eq("oldPassword"), eq("newPassword123"))).thenReturn(true);
        
        mockMvc.perform(put("/api/users/current/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    public void testChangePasswordWithMismatchConfirmation() throws Exception {
        // 设置确认密码不一致
        changePasswordDTO.setConfirmPassword("differentPassword");
        
        mockMvc.perform(put("/api/users/current/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is("新密码与确认密码不一致")));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateEmail() throws Exception {
        when(userService.updateEmail(
                eq("testuser"), 
                eq("newemail@example.com"), 
                eq("123456"), 
                eq("password123")
        )).thenReturn(userVO);
        
        mockMvc.perform(put("/api/users/current/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.username", is("testuser")));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    public void testUploadAvatar() throws Exception {
        String avatarUrl = "https://example.com/avatars/testuser/avatar.jpg";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        // 模拟上传并更新头像的服务方法
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("avatarUrl", avatarUrl);
        when(userService.uploadAndUpdateAvatar(eq("testuser"), any())).thenReturn(resultMap);
        
        mockMvc.perform(multipart("/api/users/current/avatar")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.avatarUrl", is(avatarUrl)));
    }
    
    @Test
    public void testGetBasicUserInfo() throws Exception {
        when(userService.getBasicUserInfo(1L)).thenReturn(userVO);
        
        mockMvc.perform(get("/api/users/basic/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.username", is("testuser")))
                .andExpect(jsonPath("$.data.nickname", is("测试用户")));
    }
} 