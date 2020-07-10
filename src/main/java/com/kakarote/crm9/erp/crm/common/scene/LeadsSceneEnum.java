package com.kakarote.crm9.erp.crm.common.scene;

import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.entity.CrmScene;

import java.util.Collections;
import java.util.List;

/**
 * Leads Scene Enum
 *
 * @author hao.fu
 * @since 2019/12/27 15:47
 */
public enum LeadsSceneEnum implements ISceneEnum {
    /**
     * 线索页面的场景
     */
    ALL(0, "全部线索", true,true,false),
    MINE(1, "我的线索", false,true,false),
    DEPARTMENT_POOL(2, "部门线索池", false,true,false),
    PUBLIC_POOL(3, "线索公海", false,true,false),
    TRANSFERRED_LEADS(4, "已转化线索", false,true,false);

    private CrmScene crmScene;

    LeadsSceneEnum(int id, String name, boolean isDefault,boolean isPc,boolean isMobile) {
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
