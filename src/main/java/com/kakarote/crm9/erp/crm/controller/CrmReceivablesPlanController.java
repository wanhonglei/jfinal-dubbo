package com.kakarote.crm9.erp.crm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.erp.crm.service.CrmReceivablesPlanService;
import com.kakarote.crm9.erp.crm.service.CrmRecordService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import java.util.List;

/**
 * Receivables Plan Controller
 *
 * @author yue.li
 */
public class CrmReceivablesPlanController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private CrmReceivablesPlanService receivablesPlanService;

    @Inject
    private CrmRecordService crmRecordService;


    /**
     * 添加或修改回款计划
     * @author zxy
     */
    @Permissions("crm:receivables_plan:save")
    public void saveAndUpdate(){
        try{
            JSONObject jsonObject = JSONObject.parseObject(getRawData());
            CrmReceivablesPlan crmReceivablesPlan = jsonObject.getObject("entity", CrmReceivablesPlan.class);
            Long userId = BaseUtil.getUser() == null? null: BaseUtil.getUserId();
            R r = receivablesPlanService.saveAndUpdate(jsonObject, BaseUtil.getUserId());

            if(r.isSuccess()){
                if(crmReceivablesPlan.getReceivablesId() != null){
                    CrmReceivablesPlan oldPlan = new CrmReceivablesPlan().dao().findById(crmReceivablesPlan.getPlanId());
                    crmRecordService.updateRecord(oldPlan, crmReceivablesPlan, CrmEnum.BUSINESS_TYPE_KEY.getTypes(), userId);
                }else{
                    JSONObject resultJsonObject = (JSONObject) JSON.toJSON(r.get("data"));
                    CrmReceivablesPlan newPlan = new CrmReceivablesPlan().dao().findById(resultJsonObject.get("plan_id"));
                    crmRecordService.addRecord(newPlan.getBusinessId(), CrmEnum.RECEIVABLES_PLAN_TYPE_KEY.getTypes(), CrmEnum.BUSINESS_TYPE_KEY.getTypes(), userId);
                }
            }
            renderJson(r);
        }catch (Exception e){
            logger.error(String.format("saveAndUpdate receivablesPlan msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
    /**
     * @author zxy
     * 查询回款计划自定义字段
     */
    public void queryField(){
        renderJson(R.ok().put("data",receivablesPlanService.queryField()));
    }

    /**
     * 根据合同id和客户id查询未使用的回款计划
     * @author zxy
     */
    public void queryByContractAndCustomer(@Para("") CrmReceivablesPlan receivablesPlan){
        try{
            renderJson(receivablesPlanService.queryByContractAndCustomer(receivablesPlan));
        }catch (Exception e){
            logger.error(String.format("queryByContractAndCustomer receivablesPlan msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 删除回款计划
     */
    @Permissions("crm:receivables_plan:delete")
    public void deleteById(@Para("planId")Integer planId){
        try{
            renderJson(receivablesPlanService.deleteById(planId));
        }catch (Exception e){
            logger.error(String.format("deleteByIds receivablesPlan msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询回款计划
     * @author zxy
     */
    @Permissions("crm:receivables_plan:read")
    public void information(@Para("planId")Integer planId){
        try{
            List<Record> list = receivablesPlanService.information(planId);
            renderJson(R.ok().put("data",list));
        }catch (Exception e){
            logger.error(String.format("saveAndUpdate receivablesPlan msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询回款计划
     */
    @Permissions("crm:receivables_plan:read")
    public void getById(@Para("planId")Integer planId){
        try{
            Record record = receivablesPlanService.getById(planId);
            renderJson(R.ok().put("data",record));
        }catch (Exception e){
            logger.error(String.format("saveAndUpdate receivablesPlan msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

}
