package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.AdminUserRole;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerExt;
import com.kakarote.crm9.erp.crm.vo.CustomerWithUserNameVO;
import com.kakarote.crm9.utils.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/4/10 6:33 下午
 */
public class CrmCustomerForEsbService {

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private AdminUserService adminUserService;

    /**
     * 根据网站客户id查询CRM客户信息
     * @param siteMemberId 官网id
     */
    public R queryCrmCustomerInfo(Long siteMemberId,String mobileNo) {
        Record customerRecord = crmCustomerService.getBySiteMemberId(siteMemberId);

        Integer userId = null,deptId = null,customerId = null;
        if (customerRecord == null) {
            //通过手机号查询客户信息
            customerRecord = Db.findFirst(Db.getSql("crm.customer.queryCustomerBdAndDeptByMobile"), mobileNo);
        }

        if (customerRecord != null){
            userId = customerRecord.getInt("owner_user_id");
            deptId = customerRecord.getInt("dept_id");
            customerId = customerRecord.getInt("customer_id");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId",userId);
        jsonObject.put("userName",null);
        jsonObject.put("deptId",deptId);
        jsonObject.put("deptName",null);

        if (userId != null){
            AdminUser adminUser = AdminUser.dao.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), userId);
            if (adminUser == null){
                return R.error("userId：" + userId + "，在admin_user中不存在");
            }

            deptId = Integer.valueOf(adminDeptService.getBusinessDepartmentByDeptId(adminUser.getDeptId().toString()));
            AdminDept adminDept = AdminDept.dao.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), deptId);
            if (adminDept == null){
                return R.error("deptId：" + deptId + "，在admin_dept中不存在");
            }

            jsonObject.put("userId",userId);
            jsonObject.put("userName",adminUser.getRealname());
            jsonObject.put("deptId",deptId);
            jsonObject.put("deptName",adminDept.getName());
        }else if (deptId != null){
            AdminDept adminDept = AdminDept.dao.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), deptId);
            if (adminDept == null){
                return R.error("deptId：" + deptId + "，在admin_dept中不存在");
            }

            jsonObject.put("deptId",deptId);
            jsonObject.put("deptName",adminDept.getName());
        }else if(customerId != null){
            CrmCustomerExt customerExt = CrmCustomerExt.dao.findFirst(Db.getSql("crm.customerExt.queryByCustomerId"), customerId);
            //判断是否领取中，产品同意默认值
            if (customerExt != null && customerExt.getDrawFlag() != 0){
                jsonObject.put("deptName","领取审批中");
            }
        }

        return Objects.requireNonNull(R.ok().put("code", 200)).put("data", jsonObject);
    }

    /**
     * 根据域账号查询 我负责的、我参与的、下属负责的 三个场景的客户数据
     * @param username
     */
    public List<CustomerWithUserNameVO> queryCustomerByUsername(String username){
        AdminUser adminUser = AdminUser.dao.findSingleByColumn("username", username);
        if (adminUser == null){
            throw new CrmException("域账号：" + username + "，查不到用户");
        }
        Long userId = adminUser.getUserId();

        List<AdminUserRole> userRoleList = AdminUserRole.dao.findListByColumn("user_id", userId);

        List<Long> userIds;
        if (userRoleList.stream().anyMatch(o -> ObjectUtil.equal(o.getRoleId(),BaseConstant.SUPER_ADMIN_ROLE_ID))) {
            userIds = adminUserService.queryUserByParentUser(userId, BaseConstant.AUTH_DATA_RECURSION_NUM);
            userIds.add(userId);
        } else {
            userIds = adminUserService.queryUserByAuth(userId);
        }

        List<Record> recordList = CrmCustomer.dao.queryCustomerWithOwnerAndGroupMember(userIds, userId);

        return new ArrayList<>(recordList.stream().collect(Collectors.toMap(o1 -> o1.getLong("customer_id"),
                o2 -> {
                    CustomerWithUserNameVO.SiteMemberInfo siteMemberInfo = new CustomerWithUserNameVO.SiteMemberInfo();
                    siteMemberInfo.setSiteMemberId(o2.getInt("site_member_id"));
                    siteMemberInfo.setSiteMemberName(o2.getStr("site_member_name"));

                    CustomerWithUserNameVO customer = new CustomerWithUserNameVO();

                    customer.setCustomerId(o2.getLong("customer_id"));
                    customer.setCustomerName(o2.getStr("customer_name"));
                    customer.setOwnerUserId(o2.getInt("owner_user_id"));
                    customer.setOwnerUserName(o2.getStr("owner_user_name"));
                    customer.setDeptId(o2.getInt("dept_id"));
                    customer.setDeptName(o2.getStr("dept_name"));

                    customer.setSiteMemberList(Collections.singletonList(siteMemberInfo));
                    return customer;
                },
                (CustomerWithUserNameVO customer1, CustomerWithUserNameVO customer2) -> {
                    List<CustomerWithUserNameVO.SiteMemberInfo> siteMemberList = new LinkedList<>();
                    siteMemberList.addAll(customer1.getSiteMemberList());
                    siteMemberList.addAll(customer2.getSiteMemberList());

                    customer1.setSiteMemberList(siteMemberList);
                    return customer1;
                }
        )).values());
    }
}
