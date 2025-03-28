package com.zhangziqi.online_course_mine.model.converter;

import com.zhangziqi.online_course_mine.model.dto.QuotaApplicationDTO;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.QuotaApplication;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.vo.QuotaApplicationVO;

/**
 * 存储配额申请转换器
 */
public class QuotaApplicationConverter {
    
    /**
     * DTO转实体
     */
    public static QuotaApplication toEntity(QuotaApplicationDTO dto, String applicationId, User applicant) {
        return QuotaApplication.builder()
                .applicationId(applicationId)
                .institutionId(applicant.getInstitutionId())
                .applicantId(applicant.getId())
                .quotaType(dto.getQuotaType())
                .requestedBytes(dto.getRequestedBytes())
                .reason(dto.getReason())
                .status(0) // 待审核
                .build();
    }
    
    /**
     * 实体转VO（简单转换，不包含用户名和机构名）
     */
    public static QuotaApplicationVO toVO(QuotaApplication application) {
        return QuotaApplicationVO.builder()
                .id(application.getId())
                .applicationId(application.getApplicationId())
                .institutionId(application.getInstitutionId())
                .applicantId(application.getApplicantId())
                .quotaType(application.getQuotaType())
                .requestedBytes(application.getRequestedBytes())
                .reason(application.getReason())
                .status(application.getStatus())
                .reviewerId(application.getReviewerId())
                .reviewedAt(application.getReviewedAt())
                .reviewComment(application.getReviewComment())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
    
    /**
     * 实体转VO（包含用户名和机构名）
     */
    public static QuotaApplicationVO toVO(QuotaApplication application, Institution institution, 
                                          User applicant, User reviewer) {
        QuotaApplicationVO vo = toVO(application);
        
        if (institution != null) {
            vo.setInstitutionName(institution.getName());
        }
        
        if (applicant != null) {
            vo.setApplicantUsername(applicant.getUsername());
        }
        
        if (reviewer != null) {
            vo.setReviewerUsername(reviewer.getUsername());
        }
        
        return vo;
    }
} 