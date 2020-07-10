package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.admin.entity.AdminBusinessStatusType;
import com.kakarote.crm9.erp.admin.service.AdminBusinessStatusTypeService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import java.util.List;

/**
 * 阶段类别设置
 *
 * @author yue.li
 */
public class AdminBusinessStatusTypeController extends Controller {

    @Inject
    private AdminBusinessStatusTypeService adminBusinessStatusTypeService;

    private Log logger = Log.getLog(getClass());

    /**
     * 阶段类别设置
     * @author yue.li
     * return
     */
    public void addOrUpdateBusinessStatusType() {
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            logger.info(String.format("addOrUpdateBusinessStatusType方法json %s",jsonObject.toJSONString()));
            AdminBusinessStatusType adminBusinessStatusType = jsonObject.getObject("entity", AdminBusinessStatusType.class);
            renderJson(adminBusinessStatusTypeService.addOrUpdateBusinessStatusType(adminBusinessStatusType,Long.valueOf(BaseUtil.getUserId().intValue())));
        }catch (Exception e){
            logger.error(String.format("addOrUpdateBusinessStatusType adminBusinessStatusType msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询阶段类别列表
     * @author yue.li
     * return
     */
    public void queryBusinessStatusTypeList() {
        try{
            renderJson(R.ok().put("data", adminBusinessStatusTypeService.queryBusinessStatusTypeList()));
        }catch (Exception e){
            logger.error(String.format("queryBusinessStatusTypeList adminBusinessStatusType msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询阶段类别未封存列表
     * @author yue.li
     * return
     */
    public void queryBusinessStatusTypeUnSealedList() {
        try{
            renderJson(R.ok().put("data", adminBusinessStatusTypeService.queryBusinessStatusTypeUnSealedList()));
        }catch (Exception e){
            logger.error(String.format("queryBusinessStatusTypeUnSealedList adminBusinessStatusType msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 封存阶段类别
     * @author yue.li
     * return
     */
    public void sealById() {
        try{
            String ids = getPara("ids");
            renderJson(adminBusinessStatusTypeService.sealById(ids));
        }catch (Exception e){
            logger.error(String.format("sealById adminBusinessStatusType msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 解封阶段类别
     * @author yue.li
     * return
     */
    public void unSealById() {
        try{
            String ids = getPara("ids");
            renderJson(adminBusinessStatusTypeService.unSealById(ids));
        }catch (Exception e){
            logger.error(String.format("unSealById adminBusinessStatusType msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

}
