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
        return toVO(institution, true);
    }
    
    /**
     * 将实体转换为VO，可控制是否包含敏感信息
     *
     * @param institution 机构实体
     * @param includeRegisterCode 是否包含注册码
     * @return 机构VO
     */
    public static InstitutionVO toVO(Institution institution, boolean includeRegisterCode) {
        if (institution == null) {
            return null;
        }
        
        InstitutionVO vo = new InstitutionVO();
        BeanUtils.copyProperties(institution, vo);
        
        // 如果不包含注册码，则将其置为null
        if (!includeRegisterCode) {
            vo.setRegisterCode(null);
        }
        
        return vo;
    }
} 