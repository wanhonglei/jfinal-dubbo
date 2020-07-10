package com.kakarote.crm9.erp.crm.common.scene;

import com.google.common.collect.Lists;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.entity.CrmScene;
import java.util.List;

/**
 * Sign In Scene Enum
 *
 * @author hao.fu
 * @since 2019/12/31 9:26
 */
public enum SignInSceneEnum implements ISceneEnum {
    /**
     * 签到页面的场景
     */
    ALL(0, "全部签到", false,true,true),
    MINE(1, "我的签到", true,true,true);

    private CrmScene crmScene;

    SignInSceneEnum(int id, String name, boolean isDefault,boolean isPc,boolean isMobile) {
        crmScene = new CrmScene(id, name, isDefault,isPc,isMobile);
    }

    @Override
    public CrmScene getCrmScene() {
        return crmScene;
    }

    /**
     * 获取权限用户集合
     * @author yue.li
     * @param sceneId 场景ID
     * @param crmUser 登录用户对象
     */
    @Override
    public List<Integer> getSceneAuthorizedUserIds(Integer sceneId, CrmUser crmUser){
        List<Integer> authorizedUserIds = Lists.newArrayList();

        if(SignInSceneEnum.ALL.ordinal() == sceneId) {
            authorizedUserIds = crmUser.getAuthorizedUserIds();
        } else if(SignInSceneEnum.MINE.ordinal() == sceneId) {
            authorizedUserIds.add(crmUser.getCrmAdminUser().getUserId().intValue());
        }
        return authorizedUserIds;
    }
}
