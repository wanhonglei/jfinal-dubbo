package com.kakarote.crm9.erp.crm.common.scene;

import com.google.common.collect.Lists;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.entity.CrmScene;

import java.util.List;

/**
 * Notes Scene Enum
 *
 * @author hao.fu
 * @since 2019/12/27 15:52
 */
public enum NotesSceneEnum implements ISceneEnum {
    /**
     * 小记页面的场景
     */
    ALL(0, "全部", false,true,true),
    MINE(1, "我记录的", true,true,true),
    TO_ME(2, "我收到的", false,true,true);

    private CrmScene crmScene;

    NotesSceneEnum(int id, String name, boolean isDefault,boolean isPc,boolean isMobile) {
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
    public List<Integer> getSceneAuthorizedUserIds(Integer sceneId, CrmUser crmUser) {
        List<Integer> authorizedUserIds = Lists.newArrayList();

        if(NotesSceneEnum.ALL.ordinal() == sceneId) {
            authorizedUserIds = crmUser.getAuthorizedUserIds();
        } else if(NotesSceneEnum.MINE.ordinal() == sceneId || NotesSceneEnum.TO_ME.ordinal() == sceneId) {
            authorizedUserIds.add(crmUser.getCrmAdminUser().getUserId().intValue());
        }
        return authorizedUserIds;
    }

    /**
     * 包含我收到的
     * @author yue.li
     * @param sceneId 场景ID
     */
    public static boolean isContainsMyReceive(Integer sceneId) {
       return Integer.valueOf(NotesSceneEnum.ALL.getCrmScene().getSceneId()).equals(sceneId);
    }

    /**
     * 我收到的
     * @author yue.li
     * @param sceneId 场景ID
     */
    public static boolean isOnlyMyReceive(Integer sceneId){
        return Integer.valueOf(NotesSceneEnum.TO_ME.getCrmScene().getSceneId()).equals(sceneId);
    }

}
