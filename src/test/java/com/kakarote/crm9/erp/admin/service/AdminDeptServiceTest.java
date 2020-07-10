package com.kakarote.crm9.erp.admin.service;

import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.acl.department.CrmDepartmentTree;
import com.kakarote.crm9.erp.crm.acl.department.Node;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.R;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Date;
import java.util.List;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:hao.fu Created by hao.fu on 2019/6/27.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminDeptServiceTest extends BaseTest {

    private AdminDeptService adminDeptService = Aop.get(AdminDeptService.class);

    @Test
    public void getAllDeptIds() {
        List<Record> recordList = adminDeptService.getAllDeptIds();
        Assert.assertTrue(recordList.size() > 0);
    }

    @Test
    public void batchInsertDepts() {
        try {
            List<AdminDept> depts = Lists.newArrayList();
            AdminDept adminDept = new AdminDept();
            adminDept.setDeptId(11223344L);
            adminDept.setCreateTime(new Date());
            adminDept.setPid(0);
            adminDept.setDeptLevel(1);
            adminDept.setName("单元测试部门");
            adminDept.setRemark("单元测试部门");
            adminDept.setIsDelete(CrmConstant.DELETE_FLAG_NO);
            adminDept.setUpdateTime(new Date());
            adminDept.setLeaderId("");
            depts.add(adminDept);

            adminDeptService.batchInsertDepts(depts);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void batchUpdateDepts() {
        List<AdminDept> depts = Lists.newArrayList();
        AdminDept adminDept = new AdminDept();
        adminDept.setDeptId(11223344L);
        adminDept.setPid(0);
        adminDept.setDeptLevel(1);
        adminDept.setName("单元测试部门123");
        adminDept.setRemark("单元测试部门123");
        adminDept.setIsDelete(CrmConstant.DELETE_FLAG_NO);
        adminDept.setLeaderId("123");
        depts.add(adminDept);

        boolean result = adminDeptService.batchUpdateDepts(depts);
        Assert.assertTrue(result);
    }

    @Test
    public void batchUpdateDeleteFlag() {
        List<Long> ids = Lists.newArrayList();
        ids.add(11223344L);
        boolean result = adminDeptService.batchUpdateDeleteFlag(ids, CrmConstant.DELETE_FLAG_YES);
        Assert.assertTrue(result);
    }

    @Test
    public void deleteDept() {
        String deptId = "11223344";
        R r = adminDeptService.deleteDept(deptId);
    }

    @Test
    public void testBuildDeptartmentTree() {
        Node<AdminDept, List<AdminUser>> tree = CrmDepartmentTree.getCrmDepartmentTree();
        List<Node<AdminDept, List<AdminUser>>> nodes = tree.getAllLeaves();
        Node<AdminDept, List<AdminUser>> leaf = tree.getLeafNodeById(195);
        leaf.isLeafNode(8);

        // all staffs
        List<AdminUser> staffs = Lists.newArrayList();
        nodes.stream().filter(item -> item.getNodeDataValue() != null).forEach(item -> staffs.addAll(item.getNodeDataValue()));
        System.out.println(staffs.size());

        Assert.assertTrue(nodes.size() > 0);
    }

    @Test
    public void queryBizDeptTree() {
        System.out.println(adminDeptService.queryBizDeptTree());
    }

    @Test
    public void queryBusinessDeptList() {
        System.out.println(adminDeptService.queryBusinessDeptList());
    }

    @Test
    public void queryDeptList() {
        System.out.println(adminDeptService.queryDeptList());
    }

    @Test
    public void listByPid() {
        System.out.println(adminDeptService.listByPid(186));
    }

    @Test
    public void queryAllSonDeptIds() {
        System.out.println(adminDeptService.queryAllSonDeptIds(186L));
    }

    @Test
    public void getPath() {
        System.out.println(adminDeptService.getDeptNamePath(329L));
        System.out.println(adminDeptService.getDeptNamePath(310L));
        System.out.println(adminDeptService.getDeptNamePath(320L));
    }

    @Test
    public void getBusinessDeptByDeptId(){
        adminDeptService.getBusinessDeptByDeptId(186L);
    }
}
