package com.kakarote.crm9.erp.crm.acl.dataauth;

import com.google.common.collect.Lists;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.acl.department.CrmDepartmentTree;
import com.kakarote.crm9.erp.crm.acl.department.Node;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DEPARTMENT_TYPE_KEY("本部门", 3)
 *
 * @author hao.fu
 * @since 2019/11/21 18:32
 */
public class DataAuthDepartment implements IDataAuth {

    @Override
    public List<Integer> getAuthorizedUserIds(AdminUser user) {
        List<AdminUser> users = CrmDepartmentTree.getDirectStaffsInDepartment(user.getDeptId());
        return CollectionUtils.isEmpty(users) ? Collections.emptyList() : users.stream().map(AdminUser::getUserId).map(Long::intValue).collect(Collectors.toList());
    }

    @Override
    public List<Integer> getAuthorizedDeptIds(AdminUser user) {
        List<Integer> ids = Lists.newArrayList();
        Node<AdminDept, List<AdminUser>> node = CrmDepartmentTree.getDepartmentById(user.getDeptId());
        ids.add(node.getNodeId());
        return ids;
    }
}