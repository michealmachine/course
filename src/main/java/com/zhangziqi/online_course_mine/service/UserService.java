package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.RegisterDTO;
import com.zhangziqi.online_course_mine.model.dto.UserDTO;
import com.zhangziqi.online_course_mine.model.dto.UserQueryDTO;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import org.springframework.data.domain.Page;

import java.util.List;
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
} 