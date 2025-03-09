package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.RegisterDTO;
import com.zhangziqi.online_course_mine.model.dto.UserDTO;
import com.zhangziqi.online_course_mine.model.dto.UserQueryDTO;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 注册用户
     *
     * @param registerDTO 注册请求
     * @return 用户信息
     */
    User register(RegisterDTO registerDTO);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 更新最后登录时间
     *
     * @param username 用户名
     */
    void updateLastLoginTime(String username);
    
    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件
     * @return 用户列表（分页）
     */
    Page<UserVO> getUserList(UserQueryDTO queryDTO);
    
    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getUserById(Long id);
    
    /**
     * 创建用户
     *
     * @param userDTO 用户信息
     * @return 创建后的用户信息
     */
    UserVO createUser(UserDTO userDTO);
    
    /**
     * 更新用户
     *
     * @param id 用户ID
     * @param userDTO 用户信息
     * @return 更新后的用户信息
     */
    UserVO updateUser(Long id, UserDTO userDTO);
    
    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);
    
    /**
     * 修改用户状态
     *
     * @param id 用户ID
     * @param status 状态（0-禁用，1-正常）
     * @return 更新后的用户信息
     */
    UserVO updateUserStatus(Long id, Integer status);
    
    /**
     * 给用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 更新后的用户信息
     */
    UserVO assignRoles(Long userId, Set<Long> roleIds);
    
    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     */
    void batchDeleteUsers(List<Long> ids);
    
    /**
     * 获取当前登录用户信息
     *
     * @param username 当前登录用户名
     * @return 用户详细信息
     */
    UserVO getCurrentUser(String username);
    
    /**
     * 更新当前用户个人信息
     *
     * @param username 当前登录用户名
     * @param nickname 昵称
     * @param phone 手机号
     * @return 更新后的用户信息
     */
    UserVO updateCurrentUserProfile(String username, String nickname, String phone);
    
    /**
     * 修改当前用户密码
     *
     * @param username 当前登录用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(String username, String oldPassword, String newPassword);
    
    /**
     * 更新当前用户头像
     *
     * @param username 当前登录用户名
     * @param avatarUrl 头像URL
     * @return 更新后的用户信息
     */
    UserVO updateAvatar(String username, String avatarUrl);
    
    /**
     * 上传并更新用户头像
     *
     * @param username 当前登录用户名
     * @param file 头像文件
     * @return 头像URL和用户信息的映射
     * @throws IOException 文件处理异常
     */
    Map<String, String> uploadAndUpdateAvatar(String username, MultipartFile file) throws IOException;
    
    /**
     * 更新当前用户邮箱
     *
     * @param username 当前登录用户名
     * @param newEmail 新邮箱
     * @param emailCode 邮箱验证码
     * @param password 当前密码 (用于安全验证)
     * @return 更新后的用户信息
     */
    UserVO updateEmail(String username, String newEmail, String emailCode, String password);
    
    /**
     * 获取用户基本信息（用于前端展示）
     *
     * @param userId 用户ID
     * @return 用户基本信息
     */
    UserVO getBasicUserInfo(Long userId);
} 