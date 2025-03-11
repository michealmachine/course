package com.zhangziqi.online_course_mine.model.converter;

import com.zhangziqi.online_course_mine.model.entity.InstitutionApplication;
import com.zhangziqi.online_course_mine.model.vo.InstitutionApplicationVO;
import org.springframework.beans.BeanUtils;

/**
 * 机构申请转换器
 */
public class InstitutionApplicationConverter {

    /**
     * 将实体转换为VO
     *
     * @param application 机构申请实体
     * @return 机构申请VO
     */
    public static InstitutionApplicationVO toVO(InstitutionApplication application) {
        if (application == null) {
            return null;
        }
        
        InstitutionApplicationVO vo = new InstitutionApplicationVO();
        BeanUtils.copyProperties(application, vo);
        return vo;
    }
} 