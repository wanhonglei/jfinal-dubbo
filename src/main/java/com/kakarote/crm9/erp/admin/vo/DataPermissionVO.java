package com.kakarote.crm9.erp.admin.vo;

import com.kakarote.crm9.erp.crm.acl.dataauth.CrmDataAuthEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据权限对象
 * @Author: haihong.wu
 * @Date: 2020/6/23 1:42 下午
 */
@Data
public class DataPermissionVO implements Serializable {
    private static final long serialVersionUID = 873608569265722388L;

    /**
     * 数据权限级别
     * {@link CrmDataAuthEnum}
     */
    private Integer level;

    /**
     * 有权限查看的用户ID
     */
    private List<Long> userIdList;
}
