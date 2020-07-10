package com.kakarote.crm9.integration.controller;

import static com.kakarote.crm9.erp.crm.constant.CrmConstant.EMAIL_SUBJECT_PAYMENT;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.shade.io.netty.util.internal.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.erp.crm.common.CrmPayTypeEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.BopsPayment;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerPaymentChannel;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.integration.common.PaymentTypeEnum;
import com.kakarote.crm9.integration.entity.BopsPaymentInfo;
import com.kakarote.crm9.integration.entity.PaymentDetail;
import com.kakarote.crm9.integration.entity.UpdatePaymentRequest;
import com.kakarote.crm9.integration.service.BopsPaymentService;
import com.kakarote.crm9.utils.R;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Bops Payment Information Controller
 *
 * @author hao.fu
 * @create 2019/10/10 15:03
 */
@Before(IocInterceptor.class)
@Slf4j
public class BopsPaymentInfoController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Autowired
    private EsbConfig esbConfig;

    @Autowired
    private NotifyService notifyService;

    @Inject
    private AdminSendEmailService adminSendEmailService;

    @Inject
    private BopsPaymentService bopsPaymentService;

    @Inject
    private CrmCustomerService crmCustomerService;

    @Autowired
    private VelocityEngine velocityEngine;

    private final String syncHourInterval = JfinalConfig.crmProp.get("bops.payment.sync.hour.interval");
    private final String syncBopsPaymentStartParameter = JfinalConfig.crmProp.get("bops.payment.sync.start.parameter");
    private final String syncBopsPaymentEndParameter = JfinalConfig.crmProp.get("bops.payment.sync.end.parameter");
	// bops地址
	private  static final String DUAL_ADDRESS = JfinalConfig.crmProp.get("receivables.mail.dual.address");
	// 重复客户收件人邮箱，多个邮箱以","分割
	private  static final String TOEMAILS = JfinalConfig.crmProp.get("receivables.mail.dual.toEmails");

    public void retrievePaymentFromBops() {
        try {
            Map<String, String> paras = prepareQueryParas();
            String rsps = HttpKit.get(esbConfig.getBopsPaymentListUrl(), paras, esbConfig.getBopsPaymentListHeader());
            renderJson(handleResponse(rsps) ? R.ok() : R.error());
        } catch (Exception e) {
            logger.error("handle result exception: " + e);
            adminSendEmailService.sendErrorMessage(e, notifyService);
        }
    }

    private Map<String, String> prepareQueryParas() throws ParseException {
        Map<String, String> map;
        Record record = Db.findFirst(Db.getSql("crm.payment.getLatestRecord"));

        if (Objects.isNull(record)) {
            map = getQueryIntervalFromNow(LocalDateTime.now().minusHours(Integer.parseInt(syncHourInterval)));
        } else {
            BopsPayment payment = new BopsPayment()._setAttrs(record.getColumns());
            Date latest = payment.getPayTime();
            if (Objects.isNull(latest)) {
                map = getQueryIntervalFromNow(LocalDateTime.now().minusHours(Integer.parseInt(syncHourInterval)));
            } else {
                Instant instant = latest.toInstant();
                LocalDateTime dbLatest = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
                map = getQueryIntervalFromNow(dbLatest);
            }
        }

        map.forEach((k,v)->logger.info(String.format("retrieve bops payment from esb, parameter: %s, value: %s", k, v)));

        return map;
    }

    private Map<String, String> getQueryIntervalFromNow(LocalDateTime startTime) {
        Map<String, String> map = Maps.newHashMap();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CrmConstant.DATE_TIME_FORMAT);
        LocalDateTime start = LocalDateTime.of(startTime.getYear(), startTime.getMonth(), startTime.getDayOfMonth(), startTime.getHour(), 0, 0);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), 0, 0);
        map.put(syncBopsPaymentStartParameter, start.format(formatter));
        map.put(syncBopsPaymentEndParameter, end.format(formatter));
        return map;
    }

    private boolean handleResponse(String rsps) throws Exception {
        if (StringUtils.isEmpty(rsps)) {
            logger.info("get empty result when retrieve payment from bops at " + LocalDate.now());
            return false;
        } else {
            logger.info(String.format("retrievePaymentFromBops, response: %s", rsps));
            BopsPaymentInfo result = JSON.parseObject(rsps, BopsPaymentInfo.class);
            if (CrmConstant.SUCCESS_CODE.equals(result.getCode())) {
                return saveBopsPaymentInfo(result.getData());
            } else {
                throw new Exception(String.format("error occurs when get payment from bops, error code: %s", result.getCode()));
            }
        }
    }

    /**
     * Match CRM customer according to payment no, user id, pay name
     */
    public void matchCrmCustomer() {
        // get all un-matched payment record from bops payment table
        List<Record> unmatchedPayment = bopsPaymentService.getUnmatchedPayment();
        if (CollectionUtils.isEmpty(unmatchedPayment)) {
            return;
        }
        List<BopsPayment> unmatchedBopsPayments = unmatchedPayment.stream().map(item -> new BopsPayment()._setAttrs(item.getColumns())).collect(Collectors.toList());
        Map<BopsPayment, CrmCustomer> matchedPayments = validatePaymentWithCrmData(unmatchedBopsPayments);

        if (Objects.nonNull(matchedPayments) && matchedPayments.size() > 0) {
            // batch update payment record
            List<Record> paymentRecords = matchedPayments.keySet().stream().map(Model::toRecord).collect(Collectors.toList());
            bopsPaymentService.batchUpdateBopsPayment(paymentRecords);

            // update customer payment channel
            crmCustomerService.updateCrmCustomerPaymentChannel(matchedPayments);

            // send email notification
            matchedPayments.keySet().forEach(key -> {
                CrmCustomer customer = matchedPayments.get(key);
                if (customer.getOwnerUserId() != null) {
                    crmCustomerService.sendNotifyEmailForIncome(velocityEngine, notifyService, key, customer);
                }
            });
        }
        renderJson(R.ok());
    }

    private Map<BopsPayment, CrmCustomer> validatePaymentWithCrmData(List<BopsPayment> unmatchedBopsPayments) {
        if (CollectionUtils.isEmpty(unmatchedBopsPayments)) {
            return Collections.emptyMap();
        }

        Map<BopsPayment, CrmCustomer> result = Maps.newHashMap();
        List<Record> channels = crmCustomerService.getAllCustomerPaymentChannel();
        List<CrmCustomerPaymentChannel> channelList = CollectionUtils.isEmpty(channels) ? Lists.newArrayList() : channels.stream().map(item -> new CrmCustomerPaymentChannel()._setAttrs(item.getColumns())).distinct().collect(Collectors.toList());
        for (BopsPayment payment : unmatchedBopsPayments) {
            logger.info(String.format("matchCrmCustomer -> unmatchedBopsPayment: %s", payment.toString()));

            // 1. check payment channel
            if (CollectionUtils.isNotEmpty(channelList)) {
                List<CrmCustomerPaymentChannel> matchedChannel = channelList.stream().filter(item -> item.getParternAccount().equals(payment.getParternAccount()) && item.getPayName().equals(payment.getPayName())).distinct().collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(matchedChannel)) {
                    CrmCustomer customer = CrmCustomer.dao.findById(matchedChannel.get(0).getCrmCustomerId());
                    if (Objects.nonNull(customer)) {
                        payment.setCrmCustomerId(matchedChannel.get(0).getCrmCustomerId());
                        payment.setStatus(CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER);
                        logger.info(String.format("matched via payment channel: %s", payment.toString()));
                        result.put(payment, customer);
                        continue;
                    }
                }
            }

            // 2. check user id with site member id
            if (StringUtils.isNotEmpty(payment.getUserId())) {
                Record cust = crmCustomerService.getBySiteMemberId(Long.valueOf(payment.getUserId()));
                if (Objects.nonNull(cust)) {
                    CrmCustomer customer = new CrmCustomer()._setAttrs(cust.getColumns());
                    payment.setCrmCustomerId(customer.getCustomerId().intValue());
                    payment.setStatus(CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER);
                    logger.info(String.format("matched via site member id: %s", payment.toString()));
                    result.put(payment, customer);
                    continue;
                }
            }

            // 3. check pay name with crm customer name
            if (StringUtils.isNotEmpty(payment.getPayName())) {
                List<Record> recordList = crmCustomerService.getListByRealName(payment.getPayName());
                if (CollectionUtil.isNotEmpty(recordList) && recordList.size() == 1) {

                    Record record = recordList.get(0);
                    if (Objects.nonNull(record)) {
                        CrmCustomer customer = new CrmCustomer()._setAttrs(record.getColumns());
                        payment.setCrmCustomerId(customer.getCustomerId().intValue());
                        payment.setStatus(CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER);
                        logger.info(String.format("matched via pay name: %s", payment.toString()));
                        result.put(payment, customer);
                    }
                } else if (CollectionUtil.isNotEmpty(recordList) && recordList.size() > 1) {
                	// 匹配到多个客户，发送提醒邮件
                	List<CrmCustomer> customers = recordList.stream().map(record -> new CrmCustomer()._setAttrs(record.getColumns())).collect(Collectors.toList());
                    logger.info(String.format("匹配到多个客户", payment.toString()));
                	this.sendNotifyEmailForIncome(payment, customers);
                }
            }
        }
        return result;
    }
    
    /**
     * 发送通知邮件
     * @param payment
     * @param customers
     */
    public void sendNotifyEmailForIncome(BopsPayment payment, List<CrmCustomer> customers) {
    	if (CollectionUtils.isEmpty(customers)) {
            return;
        }
        if (!Integer.valueOf(CrmPayTypeEnum.CONSUME_KEY.getTypes()).equals(payment.getCrmPayType())) {
            //只有消费类回款才发邮件
            log.info("==sendNotifyEmailForIncome 非消费类回款无需发送邮件提醒 payment:{}==", JsonKit.toJson(payment));
            return;
        }

        List<String> toEmails = Lists.newArrayList();
        if (StringUtils.isEmpty(TOEMAILS) || TOEMAILS.split(",").length == 0) {
            // 没有配置邮件接收人
            log.info("BopsPaymentInfoController -> sendNotifyEmailForIncome 没有设置邮件接收人");
            return;
        }
        String[] toEmailArray = TOEMAILS.split(",");
        for (int i =0 ;i<toEmailArray.length; i++) {

            toEmails.add(toEmailArray[i]);
        }

        String subject = String.format(EMAIL_SUBJECT_PAYMENT, payment.getPayName());
        if (StringUtil.isNullOrEmpty(payment.getOrderNo())) {
        	subject = CrmConstant.EMAIL_SUBJECT_PAYMENT_REPEAT_NO_MASTER;
        } else {
        	subject = CrmConstant.EMAIL_SUBJECT_PAYMENT_REPEAT;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("customerName", customers.stream().map(customer -> customer.getCustomerName() + (StringUtils.isNotEmpty(customer.getOwnerUserName()) ?  "归属："+customer.getOwnerUserName() : "归属：无归属人")).collect(Collectors.joining("<br>")));
        params.put("income", payment.getTotalAmount());
        params.put("payType", Objects.nonNull(payment.getPayType()) ? PaymentTypeEnum.getByCode(payment.getPayType()).getDesc() : "");
        params.put("paymentNo", payment.getPaymentNo());
        params.put("payTime", payment.getPayTime());
        params.put("payComment", payment.getComment());
        //增加判断：若该笔支付未关联订单，则在下方增加提示：该笔回款支付未关联订单，请点此提交申请财务进行异常订单关联。（链接到http://work.wz-inc.com/workflow/request/AddRequest.jsp?workflowid=481&isagent=0&beagenter=0&f_weaver_belongto_userid=）
        //crm取数判断如下：
        //（1）pay_type=2  (银行收款)
        //（2）order_no =“null"为空
        if (Integer.valueOf(2).equals(payment.getPayType()) && StringUtil.isNullOrEmpty(payment.getOrderNo())) {
        	params.put("comment", "该笔回款支付未关联订单或合同，<a href=\""+DUAL_ADDRESS+"\">请点此进行人工干预申请</a>（若有疑问可咨询财务同事）");
        } else {
        	params.put("comment","");
        }

        StringWriter result = new StringWriter();
        VelocityContext velocityContext = new VelocityContext(params);
        velocityEngine.mergeTemplate(CrmConstant.PAYMENT_NOTIFY_TEMPLATE, "UTF-8", velocityContext, result);

        notifyService.email(subject, result.toString(), toEmails);
    }

    private boolean saveBopsPaymentInfo(List<PaymentDetail> data) {
        if (CollectionUtils.isEmpty(data)) {
            return true;
        }

        List<Record> paymentList = data.stream().map(item -> assembleBopsPayment(item).toRecord()).collect(Collectors.toList());

        List<Record> insertTarget = removeDuplicatePayments(paymentList);
        insertTarget.stream().forEach(item -> System.out.println(String.format("save bops payment to crm db: %s", item.toString())));
        return bopsPaymentService.batchSaveBopsPayment(insertTarget);
    }

    private List<Record> removeDuplicatePayments(List<Record> paymentList) {
        if (CollectionUtils.isEmpty(paymentList)) {
            return Lists.newArrayList();
        }

        List<String> paymentNoList = bopsPaymentService.getAllPaymentNos();
        if (CollectionUtils.isNotEmpty(paymentNoList) && CollectionUtils.isNotEmpty(paymentList)) {
            return paymentList.stream().filter(item -> !paymentNoList.contains(item.getStr("payment_no"))).collect(Collectors.toList());
        }
        return paymentList;
    }

    private BopsPayment assembleBopsPayment(PaymentDetail src) {
        BopsPayment payment = new BopsPayment();
        payment.setBopsPaymentId(IdUtil.simpleUUID());
        payment.setUserId(Objects.isNull(src.getUserId()) ? "" : src.getUserId().toString());
        payment.setPayName(Objects.isNull(src.getPayName()) ? "" : src.getPayName());
        payment.setTotalAmount(src.getTotalAmount());
        payment.setSeller(src.getSeller());
        payment.setParternAccount(src.getParternAccount());
        payment.setPayType(src.getPayType());
        payment.setPaymentNo(src.getPaymentNo());
        SimpleDateFormat sdf = new SimpleDateFormat(CrmConstant.DATE_TIME_FORMAT);
        try {
            payment.setPayTime(src.getPayTime() == null ? null : sdf.parse(src.getPayTime()));
        } catch (ParseException e) {
            payment.setPayTime(null);
        }
        payment.setOuterPaymentNo(src.getOuterPaymentNo());
        payment.setComment(src.getComment());

        String orderNo = src.getOrderNo();
        payment.setOrderNo(orderNo);
        payment.setCrmPayType(getCrmPayTypeFromOrderNo(orderNo));

        payment.setGmtCreate(new Date());
        payment.setGmtModified(new Date());
        payment.setIsDeleted(CrmConstant.DELETE_FLAG_NO);
        payment.setStatus(CrmConstant.PAYMENT_STATUS_UNBIND_CUSTOMER);
        payment.setCrmCustomerId(null);
        return payment;
    }

    private int getCrmPayTypeFromOrderNo(String orderNo) {
        if (StringUtils.isNotEmpty(orderNo)) {
        	// 核销单当成消费类支付数据处理
            return orderNo.startsWith(CrmConstant.PREFIX_ORDER) || orderNo.startsWith(CrmConstant.BILLING_ORDER) ? CrmConstant.CRM_PAYMENT_TYPE_CONSUME : CrmConstant.CRM_PAYMENT_TYPE_RECHARGE;
        } else {
            return CrmConstant.CRM_PAYMENT_TYPE_CONSUME;
        }
    }

    /**
     * Get updated bops payment information.
     */
    public void getUpdatedPaymentInfo() {
        try {
            UpdatePaymentRequest requestData = prepareUpdatePaymentRequest();
            logger.info("getUpdatedPaymentInfo for: " + StringUtils.join(requestData.getPaymentNoList(), ","));
            String response = HttpKit.post(esbConfig.getBopsUpdatedPaymentUrl(), null, requestData.toString(), esbConfig.getBopsUpdatedPaymentListHeader());
            renderJson(handleUpdatedPaymentResponse(response) ? R.ok() : R.error());
        } catch (Exception e) {
            logger.error("handle getUpdatedPaymentInfo result exception: " + e);
            adminSendEmailService.sendErrorMessage(e, notifyService);
        }
    }

    private UpdatePaymentRequest prepareUpdatePaymentRequest() {
        UpdatePaymentRequest request = new UpdatePaymentRequest();
        List<Record> records = bopsPaymentService.getNoOrderPayment();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> payNos = records.stream().map(item -> item.getStr("payment_no")).collect(Collectors.toList());
            request.setPaymentNoList(payNos);
        }
        return request;
    }

    private boolean handleUpdatedPaymentResponse(String response) throws Exception {
        if (StringUtils.isEmpty(response)) {
            logger.info("get empty result when handleUpdatedPaymentResponse at " + LocalDate.now());
            return false;
        } else {
            logger.info(String.format("handleUpdatedPaymentResponse, response: %s", response));
            BopsPaymentInfo result = JSON.parseObject(response, BopsPaymentInfo.class);
            if (CrmConstant.SUCCESS_CODE.equals(result.getCode())) {
                return updateBopsPaymentInfo(result.getData());
            } else {
                throw new Exception(String.format("error occurs when handleUpdatedPaymentResponse, error code: %s", result.getCode()));
            }
        }
    }

    private boolean updateBopsPaymentInfo(List<PaymentDetail> paymentDetails) {
        if (CollectionUtils.isEmpty(paymentDetails)) {
            return true;
        }
        Map<String, PaymentDetail> paymentNoMap = paymentDetails.stream().collect(Collectors.toMap(PaymentDetail::getPaymentNo, Function.identity()));
        List<Record> records = bopsPaymentService.getPaymentsByPaymentNo(new ArrayList<>(paymentNoMap.keySet()));

        if (CollectionUtils.isNotEmpty(records)) {
            List<BopsPayment> crmPayments = records.stream().map(item -> new BopsPayment()._setAttrs(item.getColumns())).collect(Collectors.toList());
            List<BopsPayment> updatedPayments = updateCrmPaymentsFromBops(crmPayments, paymentNoMap);
            List<Record> paymentRecords = updatedPayments.stream().map(Model::toRecord).collect(Collectors.toList());
            return bopsPaymentService.batchUpdateBopsPayment(paymentRecords);
        } else {
            return true;
        }
    }

    private List<BopsPayment> updateCrmPaymentsFromBops(List<BopsPayment> target, Map<String, PaymentDetail> source) {
        if (CollectionUtils.isEmpty(target) || CollectionUtils.isEmpty(source.keySet()) || target.size() != source.size()) {
            return Collections.emptyList();
        }

        List<BopsPayment> result = Lists.newArrayList();
        for (BopsPayment crmPayment : target) {
            logger.info("original target payment from crm: " + JsonKit.toJson(crmPayment));
            PaymentDetail detail = source.get(crmPayment.getPaymentNo());
            crmPayment.setOrderNo(detail.getOrderNo());
            crmPayment.setUserId("" + detail.getUserId());
            crmPayment.setPayName(Objects.isNull(detail.getPayName()) ? "" : detail.getPayName());
            crmPayment.setCrmPayType(detail.getPayType());
            crmPayment.setGmtModified(new Date());
            crmPayment.setCrmCustomerId(null);
            crmPayment.setStatus(CrmConstant.PAYMENT_STATUS_UNBIND_CUSTOMER);
            crmPayment.setCrmPayType(getCrmPayTypeFromOrderNo(detail.getOrderNo()));
            crmPayment.setComment(detail.getComment());
            crmPayment.setOuterPaymentNo(detail.getOuterPaymentNo());
            crmPayment.setParternAccount(detail.getParternAccount());

            result.add(crmPayment);

            logger.info("source payment from bops: " + JsonKit.toJson(detail));
            logger.info("after update, target payment from crm: " + JsonKit.toJson(crmPayment));
        }
        return result;
    }

}
