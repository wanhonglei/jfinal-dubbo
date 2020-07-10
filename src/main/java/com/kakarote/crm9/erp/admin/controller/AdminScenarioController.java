package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminScenario;
import com.kakarote.crm9.erp.admin.service.AdminScenarioService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

/**
 * 应用场景设置
 *
 * @author yue.li
 */
public class AdminScenarioController extends Controller {

    @Inject
    private AdminScenarioService adminScenarioService;
    private Log logger = Log.getLog(getClass());

    /**
     * 查询应用场景设置列表
     * @author yue.li
     * @param basePageRequest 分页对象
     *
     */
    public void queryScenarioList(BasePageRequest<AdminScenario> basePageRequest) {
        try{
            renderJson(R.ok().put("data", adminScenarioService.queryScenarioList(basePageRequest)));
        }catch (Exception e){
            logger.error(String.format("queryScenarioList adminScenario msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }

    }

    /**
     * 获取一级部门信息
     * @author yue.li
     */
    public void getDeptList() {
        try{
            renderJson(adminScenarioService.getDeptList());
        }catch (Exception e){
            logger.error(String.format("getDeptList adminScenario msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取商机组维护事业部信息
     * @author yue.li
     * return
     */
    public void getBusnessDeptList() {
        try{
            renderJson(adminScenarioService.getBusnessDeptList());
        }catch (Exception e){
            logger.error(String.format("getBusnessDeptList adminScenario msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }


    /**
     * 设置应用场景
     * @author yue.li
     */
    public void setScenario() {
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            renderJson(adminScenarioService.addScenario(jsonObject));
        }catch (Exception e){
            logger.error(String.format("setScenario adminScenario msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据id 删除应用场景
     * @author yue.li
     *
     */
    public void deleteByIds(){
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            renderJson(adminScenarioService.deleteByIds(jsonObject));
        }catch (Exception e){
            logger.error(String.format("deleteByIds adminScenario msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
}
