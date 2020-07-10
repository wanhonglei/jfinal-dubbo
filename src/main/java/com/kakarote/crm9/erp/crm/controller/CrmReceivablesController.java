package com.kakarote.crm9.erp.crm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.service.CrmReceivablesService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;

import java.util.List;

public class CrmReceivablesController extends Controller {

    @Inject
    private CrmReceivablesService crmReceivablesService;
    private Log logger = Log.getLog(getClass());
    /**@author zxy
     * 分页查询回款
     */
    public void  queryPage(BasePageRequest<CrmReceivables> basePageRequest){
        try{
            renderJson(R.ok().put("data",crmReceivablesService.queryPage(basePageRequest)));
        }catch (Exception e){
            logger.error(String.format("queryPage receivables msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 修改回款
     */
    @Permissions({"crm:receivables:update"})
//    @NotNullValidate(value = "receivablesId",message = "回款id不能为空")
    public void update(){
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            renderJson(crmReceivablesService.saveOrUpdate(jsonObject, BaseUtil.getUserId()));
        }catch (Exception e){
            logger.error(String.format("update receivables msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
    
    /**
     * @author zxy
     * 根据回款id查询
     */
    @Permissions("crm:receivables:read")
    @NotNullValidate(value = "receivablesId",message = "回款id不能为空")
    public void  queryById(@Para("receivablesId") Integer receivablesId){
        try{
            renderJson(crmReceivablesService.queryById(receivablesId));
        }catch (Exception e){
            logger.error(String.format("queryById receivables msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
//    /**
//     * @author zxy
//     * 根据回款id删除
//     */
//    @Permissions("crm:receivables:delete")
//    @NotNullValidate(value = "receivablesIds",message = "回款id不能为空")
//    public void  deleteByIds(@Para("receivablesIds") String receivablesIds){
//        try{
//            renderJson(crmReceivablesService.deleteByIds(receivablesIds));
//        }catch (Exception e){
//            logger.error(String.format("deleteByIds receivables msg:%s",BaseUtil.getExceptionStack(e)));
//            renderJson(R.error(e.getMessage()));
//        }
//    }

    /**
     * 根据回款id删除（解绑）
     */
    @Permissions("crm:receivables:bindcust")
    @NotNullValidate(value = "receivablesIds",message = "回款id不能为空")
    public void  delete(@Para("receivablesIds") String receivablesIds){
        try{
            renderJson(crmReceivablesService.deleteByIds(receivablesIds));
        }catch (Exception e){
            logger.error(String.format("delete receivables msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
    /**
     * 查询回款自定义字段
     * @author zxy
     */
    public void queryField(){
        renderJson(R.ok().put("data",crmReceivablesService.queryField()));
    }

    /**
     * 根据条件查询回款
     * @author zxy
     */
    public void queryListByType(@Para("type") String type,@Para("id")Integer id ){
        try{
            renderJson(R.ok().put("data",crmReceivablesService.queryListByType(type,id)));
        }catch (Exception e){
            logger.error(String.format("queryListByType receivables msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
    /**
     * 根据条件查询回款
     * @author zxy
     */
    public void queryList(@Para("") CrmReceivables receivables){
        try{
            renderJson(R.ok().put("data",crmReceivablesService.queryList(receivables)));
        }catch (Exception e){
            logger.error(String.format("queryList receivables msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }


    /**
     * 查询基本信息
     * @author chaokun.ding
     * @param id
     */
    @Permissions("crm:receivables:read")
    public void information(@Para("id")Integer id){
        try{
            List<Record> recordList= crmReceivablesService.information(id);
            renderJson(R.ok().put("data",recordList));
        }catch (Exception e){
            logger.error(String.format("information receivables msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
    
    /**
     * 查询核销信息
     * @param paymentCode
     */
    public void verificationInfo(@Para("paymentCode")String paymentCode, @Para("orderNo")String orderNo){
    	renderJson(R.ok().put("data",crmReceivablesService.verificationInfo(paymentCode, orderNo)));
    }
}
