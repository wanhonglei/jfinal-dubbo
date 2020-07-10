package com.kakarote.crm9.erp.crm.controller;

import java.util.Map;
import java.util.Objects;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.dto.CrmPaymentDto;
import com.kakarote.crm9.erp.crm.entity.BopsPayment;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmPaymentService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

/**
 * Crm Payment Controller
 *
 * @author hao.fu
 * @create 2019/10/23 19:32
 */
@Before(IocInterceptor.class)
public class CrmPaymentController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    CrmPaymentService crmPaymentService;

//    @Inject
//    private BopsPaymentService bopsPaymentService;

    @Inject
    private CrmCustomerService crmCustomerService;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private VelocityEngine velocityEngine;

    /**
     * 将回款关联商机下的回款计划
     */
    @Permissions("crm:receivables:bindplan")
    public void bindPayment() {
        try{
            JSONObject requestJson = JSONObject.parseObject(getRawData());
            crmPaymentService.bindPayment(requestJson);
            renderJson(R.ok());
        }catch (Exception e){
            logger.error(String.format("bindPayment error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 手动绑定回款到客户
     */
    @Permissions("crm:receivables:bindcust")
    public void bindCustomerManually() {
        try{
            JSONObject requestJson = JSONObject.parseObject(getRawData());
            String custId = requestJson.getString("custId");
            String bopsPaymentId = requestJson.getString("bopsPaymentId");
            R bindResult = crmPaymentService.bindCustomerManually(custId, bopsPaymentId);
            if (bindResult.isSuccess()) {
                // if bind success, add payment channel and send email
                Record paymentRecord = crmPaymentService.queryByBopsPaymentId(bopsPaymentId);
                CrmCustomer customer = CrmCustomer.dao.findById(custId);

                if (Objects.nonNull(paymentRecord) && Objects.nonNull(customer)) {
                    BopsPayment bopsPayment = new BopsPayment()._setAttrs(paymentRecord.getColumns());
                    Map<BopsPayment, CrmCustomer> map = Maps.newHashMap();
                    map.put(bopsPayment, customer);

                    // update payment channel for the customer
                    crmCustomerService.updateCrmCustomerPaymentChannel(map);
                    // send notify email to the owner of that customer
                    crmCustomerService.sendNotifyEmailForIncome(velocityEngine, notifyService, bopsPayment, customer);
                }
            }
            renderJson(bindResult);
        }catch (Exception e){
            logger.error(String.format("bindPayment error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    @Permissions("crm:receivables_plan:update")
    public void updateReceivablePlan() {
        try{
            JSONObject requestJson = JSONObject.parseObject(getRawData());
            renderJson(crmPaymentService.updateReceivablePlan(requestJson));
        }catch (Exception e){
            logger.error(String.format("bindPayment error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /***
     * 回款管理列表页查询
     * @author yue.li
     */
    @Permissions("crm:receivables:index")
    public void queryPaymentPageList(BasePageRequest<CrmPaymentDto> basePageRequest){
        try{
            renderJson(R.ok().put("data",crmPaymentService.queryPaymentPageList(basePageRequest,BaseUtil.getUserId())));
        }catch (Exception e){
            logger.error(String.format("queryPaymentPageList msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /***
     * 解绑
     * @author yue.li
     * @param bopsPaymentId 支付信息id
     */
    @Permissions("crm:receivables:bindcust")
    @NotNullValidate(value = "bopsPaymentId",message = "bops支付信息ID不能为空")
    @NotNullValidate(value = "crmCustomerId",message = "crmCustomerId不能为空")
    @NotNullValidate(value = "partnerAccount",message = "该笔支付对方的账号不能为空")
    public void relieve(@Para("bopsPaymentId") String bopsPaymentId,@Para("crmCustomerId") String crmCustomerId,@Para("partnerAccount") String partnerAccount){
        try{
            renderJson(crmPaymentService.relieve(bopsPaymentId,crmCustomerId,partnerAccount));
        }catch (Exception e){
            logger.error(String.format("relieve msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

}
