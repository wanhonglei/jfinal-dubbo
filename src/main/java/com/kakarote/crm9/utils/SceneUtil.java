package com.kakarote.crm9.utils;

import com.google.common.collect.Lists;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.common.scene.*;
import com.kakarote.crm9.erp.crm.entity.CrmScene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SceneUtil class
 *
 * @author yue.li
 * @date 2020/01/07
 */
public class SceneUtil {

    /**
     * 获取业务类型下手机端业务场景列表
     * @author yue.li
     * @param  bizType 业务类型
     * @return 场景集合
     */
    public static List<CrmScene> getMobileSceneList(Integer bizType){
        List<CrmScene> scenes = new SceneUtil().getSceneList(bizType);
        return scenes.stream().filter(CrmScene :: isMobile).collect(Collectors.toList());
    }

    /**
     * 获取业务类型下PC端业务场景列表
     * @author yue.li
     * @param  bizType 业务类型
     * @return 场景集合
     */
    public static List<CrmScene> getPcSceneList(Integer bizType){
        List<CrmScene> scenes = new SceneUtil().getSceneList(bizType);
        return scenes.stream().filter(CrmScene :: isPc).collect(Collectors.toList());
    }

    /**
     * 获取业务类型下所有场景
     * @author yue.li
     * @param  bizType 业务类型
     * @return 场景集合
     */
    private List<CrmScene> getSceneList(Integer bizType) {
        List<CrmScene> scenes = new ArrayList<>();
        CrmBizTypeEnum bizEnum = CrmBizTypeEnum.getBizEnumByBizId(bizType);
        if (Objects.nonNull(bizEnum)) {
            ISceneEnum[] sceneEnums = bizEnum.getSceneEnums();
            for (ISceneEnum scene : sceneEnums) {
                scenes.add(scene.getCrmScene());
            }
        }
        return scenes;
    }

    /**
     * 根据业务id和场景id获取授权用户id集合
     *
     * @param bizType
     * @param sceneId
     * @param crmUser
     * @return
     */
    public static List<Integer> getAuthorizedUserIdsForBizScene(Integer bizType, Integer sceneId, CrmUser crmUser) {
        List<Integer> authorizedUserIds = Lists.newArrayList();
        if(Objects.isNull(bizType) || Objects.isNull(sceneId) || Objects.isNull(crmUser)) {
            return Collections.emptyList();
        }
        CrmBizTypeEnum bizEnum = CrmBizTypeEnum.getBizEnumByBizId(bizType);
        if (Objects.nonNull(bizEnum)) {
            authorizedUserIds = bizEnum.getSceneEnums()[0].getSceneAuthorizedUserIds(sceneId, crmUser);
        }
        return authorizedUserIds;
    }

}
