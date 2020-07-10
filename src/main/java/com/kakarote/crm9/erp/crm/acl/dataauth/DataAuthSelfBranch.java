package com.kakarote.crm9.erp.crm.acl.dataauth;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ONESELF_BRANCH_TYPE_KEY("本人及下属", 2)
 *
 * @author hao.fu
 * @since 2019/11/21 18:32
 */
public class DataAuthSelfBranch implements IDataAuth {

    @Override
    public List<Integer> getAuthorizedUserIds(AdminUser user) {
        List<Long> users= Aop.get(AdminUserService.class).queryUserByParentUser(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
        return CollectionUtils.isEmpty(users) ? Collections.emptyList() : users.stream().map(Long::intValue).collect(Collectors.toList());
    }

    @Override
    public List<Integer> getAuthorizedDeptIds(AdminUser user) {
        return Collections.emptyList();
    }
}