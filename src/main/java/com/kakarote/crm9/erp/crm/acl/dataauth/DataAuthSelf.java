package com.kakarote.crm9.erp.crm.acl.dataauth;

import com.google.common.collect.Lists;
import com.kakarote.crm9.erp.admin.entity.AdminUser;

import java.util.Collections;
import java.util.List;

/**
 * CrmDataAuthEnum#ONESELF_TYPE_KEY("本人", 1)
 *
 * @author hao.fu
 * @since 2019/11/21 17:07
 */
public class DataAuthSelf implements IDataAuth {

    @Override
    public List<Integer> getAuthorizedUserIds(AdminUser user) {
        List<Integer> userIds = Lists.newArrayList();
        userIds.add(user.getUserId().intValue());
        return userIds;
    }

    @Override
    public List<Integer> getAuthorizedDeptIds(AdminUser user) {
        return Collections.emptyList();
    }
}
