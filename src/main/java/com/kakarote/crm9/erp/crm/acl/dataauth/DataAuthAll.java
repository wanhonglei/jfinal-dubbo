package com.kakarote.crm9.erp.crm.acl.dataauth;

import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.acl.department.CrmDepartmentTree;
import com.kakarote.crm9.erp.crm.acl.department.Node;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ALL_TYPE_KEY("全部", 5)
 *
 * @author hao.fu
 * @since 2019/11/21 18:33
 */
public class DataAuthAll implements IDataAuth {

    @Override
    public List<Integer> getAuthorizedUserIds(AdminUser user) {
        List<AdminUser> staffs = CrmDepartmentTree.getAllStaffs();
        return staffs.stream().map(AdminUser::getUserId).map(Long::intValue).collect(Collectors.toList());
    }

    @Override
    public List<Integer> getAuthorizedDeptIds(AdminUser user) {
        List<Node<AdminDept, List<AdminUser>>> depts = CrmDepartmentTree.getAllLeavesUnderRoot();
        return depts.stream().map(Node::getNodeId).collect(Collectors.toList());
    }
}
