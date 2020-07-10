package com.kakarote.crm9.erp.crm.common.scene;

import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.entity.CrmScene;

import java.util.Collections;
import java.util.List;

/**
 * Site Customer Scene Enum
 *
 * @author hao.fu
 * @since 2019/12/30 14:54
 */
public enum SiteCustomerSceneEnum implements ISceneEnum {
    /**
     * 网站客户池页面的场景
     */
    ALL(0, "全部客户", true, true, false),
    MINE(1, "我的客户", false, true, false);

    private CrmScene crmScene;

    SiteCustomerSceneEnum(int id, String name, boolean isDefault,boolean isPc,boolean isMobile) {
        crmScene = new CrmScene(id, name, isDefault,isPc,isMobile);
    }

    @Override
    public CrmScene getCrmScene() {
        return crmScene;
    }

    @Override
    public List<Integer> getSceneAuthorizedUserIds(Integer sceneId, CrmUser crmUser) {
        //TODO
        return Collections.emptyList();
    }
}
