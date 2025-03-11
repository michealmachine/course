package com.zhangziqi.online_course_mine.model.converter;

import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.vo.InstitutionVO;
import org.springframework.beans.BeanUtils;

/**
 * 机构转换器
 */
public class InstitutionConverter {

    /**
     * 将实体转换为VO
     *
     * @param institution 机构实体
     * @return 机构VO
     */
    public static InstitutionVO toVO(Institution institution) {
        if (institution == null) {
            return null;
        }
        
        InstitutionVO vo = new InstitutionVO();
        BeanUtils.copyProperties(institution, vo);
        return vo;
    }
} 