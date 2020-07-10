package com.kakarote.crm9.erp.crm.dto;

import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.PowerEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/14 3:48 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmGroupMemberDto implements Serializable {
    private static final long serialVersionUID = 153753466895934468L;

    /**
     * 对象IDs
     */
    private List<Long> objIds;

    /**
     * 对象类型
     * {@link CrmEnum}
     */
    private Integer objType;

    /**
     * 成员IDs
     * {@link AdminUser#getUserId()}
     */
    private List<Long> memberIds;

    /**
     * 读写权限
     * {@link PowerEnum}
     */
    private Integer power;

    /**
     * 是否变更到商机等
     */
    private String changeTypes;
}
