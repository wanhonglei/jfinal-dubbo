package com.kakarote.crm9.erp.crm.common.scene;

import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.entity.CrmScene;

import java.util.Collections;
import java.util.List;

/**
 * Business Scene Enum
 *
 * @author hao.fu
 * @since 2019/12/27 15:49
 */
public enum BusinessSceneEnum implements ISceneEnum {
    /**
     * 商机页面的场景
     */
    ALL(0, "全部商机", true,true,false),
    MINE(1, "我的商机", false,true,false);

    private CrmScene crmScene;

    BusinessSceneEnum(int id, String name, boolean isDefault,boolean isPc,boolean isMobile) {
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
