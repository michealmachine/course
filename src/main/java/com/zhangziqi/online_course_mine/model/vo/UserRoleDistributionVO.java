package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户角色分布统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleDistributionVO {
    
    /**
     * 总用户数
     */
    private Long totalUserCount;
    
    /**
     * 各角色用户分布
     */
    private List<RoleDistribution> roleDistributions;
    
    /**
     * 角色分布详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDistribution {
        /**
         * 角色ID
         */
        private Long roleId;
        
        /**
         * 角色名称
         */
        private String roleName;
        
        /**
         * 角色代码
         */
        private String roleCode;
        
        /**
         * 该角色用户数量
         */
        private Long userCount;
        
        /**
         * 占比
         */
        private Double percentage;
    }
} 