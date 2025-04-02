package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.RoleEnum;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.InstitutionMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 机构成员管理服务测试
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InstitutionMemberServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private InstitutionService institutionService;

    @InjectMocks
    private InstitutionMemberServiceImpl memberService;

    private Institution institution;
    private User adminUser;
    private User regularUser;
    private User otherUser;
    private Role institutionRole;
    private List<User> institutionMembers;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        institution = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail("admin@example.com")
                .registerCode("TESTCODE")
                .status(1)
                .build();

        // 设置角色
        institutionRole = Role.builder()
                .id(2L)
                .name("机构用户")
                .code(RoleEnum.INSTITUTION.getCode())
                .build();

        // 创建机构管理员用户
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .phone("13800000000")
                .password("password")
                .status(1)
                .institutionId(institution.getId())
                .roles(new HashSet<>())
                .build();
        adminUser.getRoles().add(institutionRole);

        // 创建普通机构成员
        regularUser = User.builder()
                .id(2L)
                .username("member")
                .email("member@example.com")
                .phone("13811111111")
                .password("password")
                .status(1)
                .institutionId(institution.getId())
                .roles(new HashSet<>())
                .build();
        regularUser.getRoles().add(institutionRole);

        // 创建其他用户（非本机构）
        otherUser = User.builder()
                .id(3L)
                .username("other")
                .email("other@example.com")
                .phone("13822222222")
                .password("password")
                .status(1)
                .institutionId(2L)
                .roles(new HashSet<>())
                .build();

        // 设置机构成员列表
        institutionMembers = new ArrayList<>();
        institutionMembers.add(adminUser);
        institutionMembers.add(regularUser);

        // 设置分页对象
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getInstitutionMembers_NoKeyword_Success() {
        // Arrange
        Page<User> userPage = new PageImpl<>(institutionMembers, pageable, institutionMembers.size());
        when(userRepository.findByInstitutionId(eq(institution.getId()), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserVO> result = memberService.getInstitutionMembers(institution.getId(), null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(adminUser.getUsername(), result.getContent().get(0).getUsername());
        assertEquals(regularUser.getUsername(), result.getContent().get(1).getUsername());
        verify(userRepository).findByInstitutionId(eq(institution.getId()), any(Pageable.class));
    }

    @Test
    void getInstitutionMembers_WithKeyword_Success() {
        // Arrange
        String keyword = "admin";
        List<User> filteredUsers = List.of(adminUser);
        Page<User> userPage = new PageImpl<>(filteredUsers, pageable, filteredUsers.size());
        
        when(userRepository.findByInstitutionIdAndKeyword(
                eq(institution.getId()), eq(keyword), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserVO> result = memberService.getInstitutionMembers(institution.getId(), keyword, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(adminUser.getUsername(), result.getContent().get(0).getUsername());
        verify(userRepository).findByInstitutionIdAndKeyword(
                eq(institution.getId()), eq(keyword), any(Pageable.class));
    }

    @Test
    void countInstitutionMembers_Success() {
        // Arrange
        when(userRepository.countByInstitutionId(institution.getId())).thenReturn(2L);

        // Act
        long count = memberService.countInstitutionMembers(institution.getId());

        // Assert
        assertEquals(2L, count);
        verify(userRepository).countByInstitutionId(institution.getId());
    }

    @Test
    void removeMember_Success() {
        // Arrange
        String operatorUsername = adminUser.getUsername();
        when(institutionService.isInstitutionAdmin(operatorUsername, institution.getId())).thenReturn(true);
        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(userRepository.findByUsername(operatorUsername)).thenReturn(Optional.of(adminUser));
        when(roleRepository.findByCode(RoleEnum.INSTITUTION.getCode())).thenReturn(Optional.of(institutionRole));

        // Act
        memberService.removeMember(institution.getId(), regularUser.getId(), operatorUsername);

        // Assert
        assertNull(regularUser.getInstitutionId());
        assertFalse(regularUser.getRoles().contains(institutionRole));
        verify(userRepository).save(regularUser);
    }

    @Test
    void removeMember_NotAdmin_ThrowsException() {
        // Arrange
        String operatorUsername = regularUser.getUsername();
        when(institutionService.isInstitutionAdmin(operatorUsername, institution.getId())).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
            memberService.removeMember(institution.getId(), adminUser.getId(), operatorUsername));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeMember_UserNotFound_ThrowsException() {
        // Arrange
        String operatorUsername = adminUser.getUsername();
        when(institutionService.isInstitutionAdmin(operatorUsername, institution.getId())).thenReturn(true);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
            memberService.removeMember(institution.getId(), 99L, operatorUsername));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeMember_UserNotInInstitution_ThrowsException() {
        // Arrange
        String operatorUsername = adminUser.getUsername();
        when(institutionService.isInstitutionAdmin(operatorUsername, institution.getId())).thenReturn(true);
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));
        when(userRepository.findByUsername(operatorUsername)).thenReturn(Optional.of(adminUser));

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
            memberService.removeMember(institution.getId(), otherUser.getId(), operatorUsername));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeMember_RemovingSelf_ThrowsException() {
        // Arrange
        String operatorUsername = adminUser.getUsername();
        when(institutionService.isInstitutionAdmin(operatorUsername, institution.getId())).thenReturn(true);
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.findByUsername(operatorUsername)).thenReturn(Optional.of(adminUser));

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
            memberService.removeMember(institution.getId(), adminUser.getId(), operatorUsername));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getMemberStats_Success() {
        // Arrange
        int maxMembers = 5; // 与实现类中的MAX_MEMBERS保持一致
        when(userRepository.countByInstitutionId(institution.getId())).thenReturn(2L);

        // Act
        Map<String, Object> stats = memberService.getMemberStats(institution.getId());

        // Assert
        assertNotNull(stats);
        assertEquals(2L, stats.get("total"));
        assertEquals(maxMembers, stats.get("limit"));
        assertEquals(3L, stats.get("available")); // 5-2=3
        verify(userRepository).countByInstitutionId(institution.getId());
    }
} 