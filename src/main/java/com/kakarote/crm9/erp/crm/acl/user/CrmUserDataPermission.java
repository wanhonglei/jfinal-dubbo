package com.kakarote.crm9.erp.crm.acl.user;

import com.google.common.collect.Lists;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.acl.dataauth.CrmDataAuthEnum;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.crm.acl.dataauth.DataAuthUtil;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Crm User Data Permission
 *
 * @author hao.fu
 * @create 2019/11/20 14:10
 */
public class CrmUserDataPermission implements Serializable {

    private static final long serialVersionUID = -4621815096027511707L;

    private List<Enum> dataAuthEnum = Lists.newArrayList();
    private AdminUser adminUser;

    public CrmUserDataPermission(AdminUser user, List<AdminRole> userRoles) {
        if (CollectionUtils.isNotEmpty(userRoles)) {
            adminUser = user;
            List<Integer> dataTypes = userRoles.stream().map(AdminRole::getDataType).collect(Collectors.toList());
            dataAuthEnum = dataTypes.stream().map(CrmDataAuthEnum::getAuthTypeByCode).collect(Collectors.toList());
        }
    }

    /**
     * Return user ids which the user is authorized.
     *
     * @return user ids which the user is authorized
     */
    public List<Integer> getAuthorizedUserIds() {
        if (CollectionUtils.isEmpty(dataAuthEnum)) {
            return Collections.emptyList();
        }
        Set<Integer> ids = new HashSet<>();
        dataAuthEnum.forEach(item -> {
            // ordinal start from 0, so we need plus 1 here
            List<Integer> authorizedUserIds = DataAuthUtil.getAuthorizedUserIds(item.ordinal() + 1, adminUser);
            if (authorizedUserIds != null){
                ids.addAll(authorizedUserIds);
            }
        });

        ids.add(adminUser.getUserId().intValue());
        return new ArrayList<>(ids);
    }

    /**
     * Return department ids which the user is authorized.
     *
     * @return department ids which the user is authorized.
     */
    public List<Integer> getAuthorizedDeptIds() {
        if (CollectionUtils.isEmpty(dataAuthEnum)) {
            return Collections.emptyList();
        }
        Set<Integer> ids = new HashSet<>();
        dataAuthEnum.forEach(item -> {
            // ordinal start from 0, so we need plus 1 here
            List<Integer> authorizedDepartmentIds = DataAuthUtil.getAuthorizedDepartmentIds(item.ordinal() + 1, adminUser);
            if (authorizedDepartmentIds != null){
                ids.addAll(authorizedDepartmentIds);
            }
        });

        ids.add(adminUser.getDeptId());
        return new ArrayList<>(ids);
    }
}
