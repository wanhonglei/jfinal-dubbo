package com.kakarote.crm9.integration.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.HttpEnum;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.config.cache.RedisCache;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmEmailConstant;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.erp.crm.service.CrmContractService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerForEsbService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmReceivablesService;
import com.kakarote.crm9.erp.crm.vo.CustomerWithUserNameVO;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CrmInterfaceForEsbController.
 *
 * @author yue.li
 * @create 2019/7/25 10:00
 */
@Before(IocInterceptor.class)
@Slf4j
public class CrmInterfaceForEsbController extends Controller {

    @Inject
    private AdminSendEmailService adminSendEmailService;

    @Autowired
    private NotifyService notifyService;

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private AdminDeptService adminDeptService;

    @Autowired
    private EsbConfig esbConfig;

    @Inject
    private RedisCache redisCache;

    @Inject
    private CrmCustomerForEsbService crmCustomerForEsbService;

    @Inject
    private CrmContractService crmContractService;

    @Inject
    private CrmReceivablesService crmReceivablesService;

    /**
     * 根据客户名称获取客户详情
     * @author yue.li
     * @param  customerName 客户名称
     */
    public void getCustomerInfoByCustomerName(@Para("customerName") String customerName) {
        if (StringUtils.isEmpty(customerName)) {
            renderJson(R.error("客户名不能为空!"));
            return;
        }
        CrmCustomer customer = new CrmCustomer();
        try{
            customer = crmCustomerService.constructCustomer(null,customerName,null,null);
        }catch(Exception e) {
            // 发送失败消息通知
            adminSendEmailService.sendErrorMessage(e,notifyService);
        }
        renderJson(R.ok().put("data", customer));
    }

    /**
     * 网站客户池领取(OA 回调)
     * @author yue.li
     */
    public void transferWebsiteCustomer() {
        try{
            log.info("transferWebsiteCustomer json:{}",getRawData());
            CrmCustomer crmCustomer = JSONObject.parseObject(getRawData(),CrmCustomer.class);

            if(Objects.isNull(crmCustomer.getCustomerId())) {
                renderJson(R.error(CrmErrorInfo.CUSTOMER_IS_NULL));
                return;
            }

            AdminUser adminUser = adminUserService.getUserByUserName(crmCustomer.getNewOwnerUserLoginId());
            if (Objects.isNull(adminUser) || Objects.isNull(adminUser.getUserId())) {
                renderJson(R.error(CrmErrorInfo.CUSTOMER_OWNER_USER_IS_NULL));
                return;
            }

            if (crmCustomer.getAuditResult() == null){
                renderJson(R.error("参数：auditResult 不能为null"));
                return;
            }

            //2 主动审核失败 3 自动审核失败
            if (crmCustomer.getAuditResult() == 2 || crmCustomer.getAuditResult() == 3){
                crmCustomerService.ClearCustomerDrawFlagAndCache(crmCustomer.getCustomerId(),adminUser.getUserId());
                CrmCustomer customer = CrmCustomer.dao.findById(crmCustomer.getCustomerId());
                if (customer == null){
                    renderJson(R.error("根据customerId 查询不到客户信息"));
                    return;
                }

                //OA审批失败，发送邮件通知申请人
                String content;
                if (crmCustomer.getAuditResult() == 2){
                    content = "您申请领取的客户：" + customer.getCustomerName() + "，部门管理层审核不通过，您本次领取失败，请知晓";
                }else {
                    content = "您申请领取的客户：" + customer.getCustomerName() + "，由于24小时内未完成审批，根据公司规则，您的本次申请，系统已自动置为审核失败，请知晓";
                }
                notifyService.email(CrmEmailConstant.APPLY_CUSTOMER_FAIL, content , Collections.singletonList(adminUser.getEmail()));
                renderJson(R.ok());
                return;
            }

            //判断回传的orderPaymentAmount是否有值
            if (crmCustomer.getOrderPaymentAmount() == null){
                renderJson(R.error("参数：orderPaymentAmount 不能为空"));
                return;
            }

            if(Objects.nonNull(adminUser.getDeptId())) {
                //如果缓存中没有申请的数据，则重新获取
                if (!redisCache.exist(CrmConstant.PERFORMANCE_USER_CUSTOMER + adminUser.getUserId() + "_" + crmCustomer.getCustomerId())){
                    crmCustomerService.getCustomerPerformanceMsg(crmCustomer.getCustomerId(), adminUser.getUserId(), esbConfig,crmCustomer.getOrderPaymentAmount());
                }
                renderJson(crmCustomerService.confirmReceiveWithIndustry(crmCustomer.getCustomerId(),crmCustomer.getIndustryCode(),adminUser.getUserId(),Long.valueOf(adminUser.getDeptId()),esbConfig));
            }else{
                renderJson(R.error(CrmErrorInfo.DEPT_IS_NULL));
            }
        } catch (CrmException e) {
            log.warn("transferWebsiteCustomer msg:{}", e.getMessage());
            renderJson(R.error(e.getMessage()));
        } catch (Exception e) {
            log.error("transferWebsiteCustomer msg:{}", BaseUtil.getExceptionStack(e));
            // 发送失败消息通知
            adminSendEmailService.sendErrorMessage(e, notifyService);
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 判断用户是否可以下单
     */
    public void checkUserCanOrder(){
        JSONObject jsonObject = JSON.parseObject(getRawData());
        String userName = jsonObject.getString("userName");
        String siteMemberId = jsonObject.getString("siteMemberId");

        if (org.apache.commons.lang3.StringUtils.isBlank(userName)){
            renderJson(R.error("参数： userName 不能为空"));
            return;
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(siteMemberId)){
            renderJson(R.error("参数： siteMemberId 不能为空"));
            return;
        }

        long siteMemberIdLong;
        try {
            siteMemberIdLong = Long.parseLong(siteMemberId);
        } catch (NumberFormatException e) {
            renderJson(R.error("参数： siteMemberId 格式错误"));
            return;
        }

        renderJson(crmCustomerService.checkUserCanOrder(userName,siteMemberIdLong));

    }

    /**
     * 查询部门和bd
     */
    public void queryDeptAndBdByUser(@Para("siteMemberId") String siteMemberId){
        if (org.apache.commons.lang3.StringUtils.isBlank(siteMemberId)){
            renderJson(R.error("参数： siteMemberId 不能为空"));
            return;
        }

        long siteMemberIdLong;
        try {
            siteMemberIdLong = Long.parseLong(siteMemberId);
        } catch (NumberFormatException e) {
            renderJson(R.error("参数： siteMemberId 格式错误"));
            return;
        }

        renderJson(crmCustomerService.queryDeptAndBdByUser(siteMemberIdLong));

    }

    /**
     * 根据网站客户id查询CRM客户信息
     * @param siteMemberId
     */
    public void queryCrmCustomerInfo(@Para("siteMemberId") String siteMemberId,@Para("mobileNo") String mobileNo){
        if (org.apache.commons.lang3.StringUtils.isBlank(siteMemberId)){
            renderJson(R.error("参数： siteMemberId 不能为空"));
            return;
        }

        if (org.apache.commons.lang3.StringUtils.isBlank(mobileNo)){
            renderJson(R.error("参数： mobileNo 不能为空"));
            return;
        }

        long siteMemberIdLong;
        try {
            siteMemberIdLong = Long.parseLong(siteMemberId);
        } catch (NumberFormatException e) {
            renderJson(R.error("参数： siteMemberId 格式错误"));
            return;
        }

        renderJson(crmCustomerForEsbService.queryCrmCustomerInfo(siteMemberIdLong,mobileNo));

    }

    /**
     * 查询回款计划
     * planIds："1，2，3"
     */
    public void queryReceivablesPlan(@Para("planIds") String planIds){
        log.info("queryReceivablesPlan planIds:{}",planIds);

        try {
            List<Long> longs = Arrays.stream(planIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
            List<CrmReceivablesPlan> receivablesPlans = CrmReceivablesPlan.dao.findListWithColValues("plan_id", longs);
            List<Record> recordList = crmReceivablesService.findProductByPlanIds(longs);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("receivablesPlanList",receivablesPlans);
            jsonObject.put("productList",recordList);

            renderJson(R.okWithData(jsonObject));
        } catch (Exception e) {
            log.error("查询回款计划方法业务异常:{}",e.getMessage());
            renderJson(R.error("参数： planIds 格式错误"));
        }
    }

    /**
     * 保存或者更新合同信息
     */
    @NotNullValidate(value = "oaNum", type = HttpEnum.JSON, message = "OA销售合同申请流程编号 不能为空")
    @NotNullValidate(value = "contractName", type = HttpEnum.JSON, message = "合同名称 不能为空")
    @NotNullValidate(value = "siteMemberId", type = HttpEnum.JSON, message = "官网用户ID 不能为空")
    @NotNullValidate(value = "businessId", type = HttpEnum.JSON, message = "商机ID 不能为空")
    @NotNullValidate(value = "contractTarget", type = HttpEnum.JSON, message = "合同目的 不能为空")
    @NotNullValidate(value = "contractType", type = HttpEnum.JSON, message = "合同类型 不能为空")
    @NotNullValidate(value = "isSecret", type = HttpEnum.JSON, message = "是否包含保密条款 不能为空")
    @NotNullValidate(value = "contractMoney", type = HttpEnum.JSON, message = "合同金额 不能为空")
    @NotNullValidate(value = "currencyType", type = HttpEnum.JSON, message = "币种 不能为空")
    @NotNullValidate(value = "checkStatus", type = HttpEnum.JSON, message = "审核状态 不能为空")
    @NotNullValidate(value = "paymentType", type = HttpEnum.JSON, message = "收款/付款方式 不能为空")
    @NotNullValidate(value = "applyUserId", type = HttpEnum.JSON, message = "申请人用户id 不能为空")
    @NotNullValidate(value = "signUserId", type = HttpEnum.JSON, message = "签订人用户id 不能为空")
    @NotNullValidate(value = "signDeptId", type = HttpEnum.JSON, message = "签订部门id 不能为空")
    @NotNullValidate(value = "requestId", type = HttpEnum.JSON, message = "流程ID 不能为空")
    public void addOrUpdateContract(){
        log.info("transferContract:{}",getRawData());
        CrmContract crmContract;
        try {
            crmContract = JSONObject.parseObject(getRawData(), CrmContract.class);
        } catch (Exception e) {
            renderJson(R.error("参数 格式错误：" + e.getMessage()));
            return;
        }

        crmContractService.addOrUpdateContract(crmContract);
        renderJson(R.ok());
    }

    /**
     * 根据域账号查询客户信息接口
     * @param username
     */
    public void queryCustomerByUsername(@Para("username") String username){
        log.info("queryCustomerByUsername username:{}",username);
        if (StringUtils.isBlank(username)){
            renderJson(R.error("参数： username 不能为空"));
            return;
        }

        List<CustomerWithUserNameVO> voList = crmCustomerForEsbService.queryCustomerByUsername(username);
        renderJson(R.okWithData(voList));
    }

}
