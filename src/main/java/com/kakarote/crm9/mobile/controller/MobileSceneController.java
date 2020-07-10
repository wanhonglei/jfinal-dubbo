package com.kakarote.crm9.mobile.controller;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.entity.CrmScene;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.SceneUtil;
import java.util.List;

/**
 * Mobile Scene Controller
 *
 * @author hao.fu
 * @since 2019/12/30 11:29
 */
@Before(IocInterceptor.class)
public class MobileSceneController extends Controller {

    private Log logger = Log.getLog(getClass());

    /**
     * 根据业务id查询对应的场景
     * @param bizType 业务类型{@code CrmBizTypeEnum}
     */
    @NotNullValidate(value = "bizType",message = "业务id不能为空")
    public void queryScene(@Para("bizType")Integer bizType) {
        try{
            List<CrmScene> scenes = SceneUtil.getMobileSceneList(bizType);
            renderJson(R.ok().put("data", JSON.toJSON(scenes)));
        }catch (Exception e){
            logger.error(String.format("MobileSceneController queryScene msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

}
