package com.kakarote.crm9.erp.crm.common.scene;

import com.google.common.collect.Lists;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.entity.CrmScene;
import java.util.List;

/**
 * Customer Scene Enum
 *
 * @author hao.fu
 * @since 2019/12/27 15:46
 */
public enum CustomerSceneEnum implements ISceneEnum {

    /**
     * 客户页面的四种场景
     */
    ALL(0, "全部客户", false,true,true),
    MINE(1, "我的客户", true,true,true),
    DEPARTMENT_POOL(2, "部门客户池", false,true,false),
    SITE_POOL(3, "网站客户池", false,true,false);

    private CrmScene crmScene;

    CustomerSceneEnum(int id, String name, boolean isDefault,boolean isPc,boolean isMobile) {
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

        if(CustomerSceneEnum.ALL.ordinal() == sceneId) {
            authorizedUserIds = crmUser.getAuthorizedUserIds();
        } else if(CustomerSceneEnum.MINE.ordinal() == sceneId) {
            authorizedUserIds.add(crmUser.getCrmAdminUser().getUserId().intValue());
        }
        return authorizedUserIds;
    }

    /**
     * 是否包含团队成员
     * @author yue.li
     * @param sceneId 场景ID
     */
    public static boolean isContainsTeamMember(Integer sceneId) {
        return Integer.valueOf(CustomerSceneEnum.MINE.getCrmScene().getSceneId()).equals(sceneId);
    }
}
