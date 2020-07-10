package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminUserRole;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.util.List;

public class AdminSceneServiceTest extends BaseTest {

    AdminSceneService sceneService = Aop.get(AdminSceneService.class);

//    @Test
//    public void sceneConfig() {
//        //联系小计隐藏场景
//        AdminScene adminScene = new AdminScene();
//        adminScene.setType(3);
//        adminScene.setNoHideIds("");
//        adminScene.setHideIds("2043,2044,2045");
//        Assert.assertNotNull(sceneService.sceneConfig(adminScene));
//    }

    @Test
    public void queryScene() {
        Db.tx(() -> {
            Long userId = 1992L;
            System.out.println(JsonKit.toJson(sceneService.queryScene(Integer.parseInt(CrmEnum.LEADS_TYPE_KEY.getTypes()), userId)));
            String roleName = "销售支持";
            Long roleId = Db.queryLong("select role_id from 72crm_admin_role where role_name=?", roleName);
            if (roleId == null) {
                return false;
            }
            List<AdminUserRole> adminUserRoles = AdminUserRole.dao.findListByColumns(new String[]{"user_id", "role_id"}, new Object[]{1992L, roleId});
            if (CollectionUtils.isEmpty(adminUserRoles)) {
                AdminUserRole adminUserRole = new AdminUserRole();
                adminUserRole.setUserId(userId);
                adminUserRole.setRoleId(roleId.intValue());
                adminUserRole.save();
            }
            mockBaseUtil("1011");
            System.out.println(JsonKit.toJson(sceneService.queryScene(Integer.parseInt(CrmEnum.LEADS_TYPE_KEY.getTypes()), userId)));
            return false;
        });
    }
}