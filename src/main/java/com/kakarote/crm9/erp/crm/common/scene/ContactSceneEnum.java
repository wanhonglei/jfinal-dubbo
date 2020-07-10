package com.kakarote.crm9.erp.crm.common.scene;

import com.google.common.collect.Lists;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.entity.CrmScene;

import java.util.List;

/**
 * Contact Scene Enum
 *
 * @author hao.fu
 * @since 2019/12/27 15:48
 */
public enum ContactSceneEnum implements ISceneEnum {
    /**
     * 联系人页面的场景
     */
    ALL(0, "全部联系人", false,true,true),
    MINE(1, "我的联系人", true,true,true);

    private CrmScene crmScene;

    ContactSceneEnum(int id, String name, boolean isDefault,boolean isPc,boolean isMobile) {
        crmScene = new CrmScene(id, name, isDefault,isPc,isMobile);
    }

    @Override
    public CrmScene getCrmScene() {
        return crmScene;
    }

    @Override
    public List<Integer> getSceneAuthorizedUserIds(Integer sceneId, CrmUser crmUser) {
        List<Integer> authorizedUserIds = Lists.newArrayList();

        if(ContactSceneEnum.ALL.ordinal() == sceneId) {
            authorizedUserIds = crmUser.getAuthorizedUserIds();
        } else if(ContactSceneEnum.MINE.ordinal() == sceneId) {
            authorizedUserIds.add(crmUser.getCrmAdminUser().getUserId().intValue());
        }
        return authorizedUserIds;
    }
}
