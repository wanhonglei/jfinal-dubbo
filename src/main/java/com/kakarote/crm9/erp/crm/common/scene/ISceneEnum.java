package com.kakarote.crm9.erp.crm.common.scene;

import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.entity.CrmScene;

import java.util.List;

/**
 * ISceneEnum
 *
 * @author hao.fu
 * @since 2019/12/30 15:48
 */
public interface ISceneEnum {
    CrmScene getCrmScene();

    List<Integer> getSceneAuthorizedUserIds(Integer sceneId, CrmUser crmUser);
}
