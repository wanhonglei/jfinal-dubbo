package com.kakarote.crm9.erp.crm.acl.dataauth;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DEPARTMENT_BRANCH_TYPE_KEY("本部门及下属部门", 4)
 *
 * @author hao.fu
 * @since 2019/11/21 18:43
 */
public class DataAuthDepartmentBranch implements IDataAuth {

    @Override
    public List<Integer> getAuthorizedUserIds(AdminUser user) {
        List<Long> users= Aop.get(AdminUserService.class).queryUserByAuth(user.getUserId());
        return CollectionUtils.isEmpty(users) ? Collections.emptyList() : users.stream().map(Long::intValue).collect(Collectors.toList());
    }

    @Override
    public List<Integer> getAuthorizedDeptIds(AdminUser user) {
       return Collections.emptyList();
    }
}