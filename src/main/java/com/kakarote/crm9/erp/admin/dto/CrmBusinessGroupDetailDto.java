package com.kakarote.crm9.erp.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 商机组详情
 *
 * @author liming.guo
 */
@Data
@Builder
public class CrmBusinessGroupDetailDto {

    /**
     * 商机组ID
     */
    private Long groupId;
    /**
     * 商机组名称
     */
    private String groupName;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 创建用户名称
     */
    private String createUserName;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新用户名称
     */
    private String updateUserName;
    /**
     * 更新时间
     */
    private String updateTime;
    /**
     * 商机组email
     */
    private String groupEmail;

}
