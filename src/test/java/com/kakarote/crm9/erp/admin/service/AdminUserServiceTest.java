package com.kakarote.crm9.erp.admin.service;

import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.R;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:hao.fu Created by hao.fu on 2019/6/28.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminUserServiceTest extends BaseTest {

    AdminUserService adminUserService = Aop.get(AdminUserService.class);

    @Test
    public void batchInsertNewUser() {
        List<AdminUser> users = Lists.newArrayList();
        AdminUser user = new AdminUser();
        user.setUserId(99887766L);
        user.setDeptId(111);
        user.setEmail("test@wz-inc.com");
        user.setHireTime(new Date());
        user.setLeaderNum("123");
        user.setParentId(1L);
        user.setNum("0765");
        user.setMobile("13333333333");
        user.setPost("测试");
        user.setSex(1);
        user.setStatus(1);
        user.setRealname("张三");
        user.setUsername("张三");
        users.add(user);
        boolean result = adminUserService.batchInsertNewUser(users);
    }

    @Test
    public void batchUpdateUserStatus() {
        List<Long> ids = Lists.newArrayList();
        ids.add(99887766L);
        adminUserService.batchUpdateUserStatus(ids, CrmConstant.DELETE_FLAG_YES);
    }

    @Test
    public void batchUpdateStaff() {
        List<AdminUser> users = Lists.newArrayList();
        AdminUser user = new AdminUser();
        user.setUserId(99887766L);
        user.setDeptId(2222);
        user.setPost("测试1111");
        users.add(user);
        adminUserService.batchUpdateStaff(users);
    }

    @Test
    public void deleteUserByUserId() {
    	
        try {
			adminUserService.deleteUserByUserId(99887766L);
		} catch (Exception e) {
            System.out.println(e.getMessage());
		}
    }

    @Test
    public void getUserByStaffNo() {
        String staffNo = "0556";
        AdminUser user = adminUserService.getUserByStaffNo(staffNo);
    }

    @Test
    public void queryUserInfos() {
        List<AdminUser> adminUserList = adminUserService.queryUserInfos();
    }

    @Test
    public void getUserIdsByUserNames() {
        List<String> userNames = new ArrayList<>();
        userNames.add("yue.li");
        List<Long> userIds = adminUserService.getUserIdsByUserNames(userNames);

    }

    @Test
    public void checkUserCustomerAndBusiness() {
        AdminUser adminUser = new AdminUser();
        adminUser.setUserId(421L);
        adminUser.setUsername("liming.guo");
        adminUser.setDeptId(186);
        adminUser.setEmail("liming.guo@wz-inc.com");
        List<Integer> roles = Lists.newArrayList();
        roles.add(1);
        adminUser.setRoles(roles);
        adminUser.setParentId(1080L);
        adminUser.setRealname("郭黎明");
        R result = adminUserService.checkUserCustomerAndBusiness(adminUser);
    }

    @Test
    public void setUser() {
        AdminUser adminUser = new AdminUser();
        adminUser.setUserId(421L);
        adminUser.setUsername("liming.guo");
        adminUser.setDeptId(186);
        adminUser.setEmail("liming.guo@wz-inc.com");
        List<Integer> roles = Lists.newArrayList();
        roles.add(1);
        adminUser.setRoles(roles);
        adminUser.setParentId(1080L);
        adminUser.setRealname("郭黎明");
        String roleIds = "1";
        R result = adminUserService.setUser(adminUser, roleIds);
    }

    @Test
    public void resetUser() {
        AdminUser adminUser = new AdminUser();
        adminUser.setUserId(2736L);
        adminUserService.resetUser(adminUser,"test:user");
    }

    @Test
    public void queryUserList() {
        AdminUser adminUser = new AdminUser();
        adminUser.setDeptId(287);
        adminUser.setRealname("万红磊");
        adminUser.setStatus(1);
        BasePageRequest<AdminUser> request = new BasePageRequest<>(1,10,adminUser);
        request.setPageType(0);
        adminUserService.queryUserList(request,"1","test");

        request.setPageType(1);
        adminUserService.queryUserList(request,"1","test");
    }

    @Test
    public void queryTopUserList() {
        adminUserService.queryTopUserList(1992L);
    }

    @Test
    public void queryChileDeptIds() {
        adminUserService.queryChileDeptIds(0,10);
    }

    @Test
    public void queryChileUserIds() {
        adminUserService.queryChileUserIds(1992L,10);
    }

    @Test
    public void resetPassword() {
        adminUserService.resetPassword("2826","test");
    }

    @Test
    public void querySuperior() {
        adminUserService.querySuperior("范吉兆");
    }

    @Test
    public void queryListName() {
        adminUserService.queryListName("范吉兆");
    }

    @Test
    public void queryListNameByDept() {
        adminUserService.queryListNameByDept("范吉兆");
    }

    @Test
    public void queryAllUserList() {
        adminUserService.queryAllUserList();
    }

    @Test
    public void setUserStatus() {
        adminUserService.setUserStatus("2736","1");
    }

    @Test
    public void updateImg() {
        adminUserService.updateImg("http://www.baidu.com",2736L);
    }

    @Test
    public void updateUser() {
        adminUserService.updateUser(AdminUser.dao.findById(3585));
    }

    @Test
    public void queryUserByAuth() {
        adminUserService.queryUserByAuth(3585L);
    }

    @Test
    public void queryUserByParentUser() {
        adminUserService.queryUserByParentUser(3585L,10);
    }

    @Test
    public void queryUserByDeptId() {
        adminUserService.queryUserByDeptId(287);
    }

    @Test
    public void queryUserByDeptIdEliminateDisabledUser() {
        adminUserService.queryUserByDeptIdEliminateDisabledUser(287);
    }

    @Test
    public void usernameEdit() {
        adminUserService.usernameEdit(2826,"yue.li","123456");
    }

    @Test
    public void getDeptInfoByUserName() {
        adminUserService.getDeptInfoByUserName("yue.li");
    }

    @Test
    public void queryMyDeptAndSubUserByDeptId() {
        adminUserService.queryMyDeptAndSubUserByDeptId(287);
    }

    @Test
    public void getUserIdsByDeptIds() {
        adminUserService.getUserIdsByDeptIds(Collections.singletonList(287));
    }

    @Test
    public void queryMyDeptAndSubDeptId() {
        adminUserService.queryMyDeptAndSubDeptId(287);
    }

    @Test
    public void getAllUsers() {
        adminUserService.getAllUsers();
    }

    @Test
    public void getUserByUserName() {
        adminUserService.getUserByUserName("yue.li");
    }

    @Test
    public void getBusinessDepartmentOfUserById() {
        adminUserService.getBusinessDepartmentOfUserById(837L);
    }

    @Test
    public void getUserListByUserIds() {
        adminUserService.getUserListByUserIds(Collections.singletonList(837L));
    }

    @Test
    public void getUserListByDeptIds() {
        adminUserService.getUserListByDeptIds(Collections.singletonList(281L));
    }

    @Test
    public void getAdminUserByUserId() {
        adminUserService.getAdminUserByUserId(837L);
    }

    @Test
    public void queryDeptIdOfUser() {
        adminUserService.queryDeptIdOfUser(837L);
    }
}
