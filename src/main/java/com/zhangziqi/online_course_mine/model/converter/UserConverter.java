package com.zhangziqi.online_course_mine.model.converter;

import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import org.springframework.beans.BeanUtils;

/**
 * 用户转换器
 */
public class UserConverter {

    /**
     * 将实体转换为VO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    public static UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        
        return vo;
    }
}
