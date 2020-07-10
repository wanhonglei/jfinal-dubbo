package com.kakarote.crm9.integration.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.erp.crm.common.CrmLeadsSendTimesEnum;
import com.kakarote.crm9.erp.crm.common.CrmLeadsTransformEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmEmailConstant;
import com.kakarote.crm9.erp.crm.entity.CrmLeads;
import com.kakarote.crm9.erp.crm.service.CrmLeadsService;
import com.kakarote.crm9.utils.R;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CrmLeadsCronController.
 *
 * @author yue.li
 * @create 2019/7/16 10:00
 */
@Before(IocInterceptor.class)
public class CrmLeadsCronController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminDeptService adminDeptService;

    @Autowired
    private NotifyService notifyService;

    @Inject
    private AdminSendEmailService adminSendEmailService;

    @Autowired
    private VelocityEngine velocityEngine;

    @Inject
    private CrmLeadsService crmLeadsService;

    /**
     * 超过N天无联系小计、超过N天未转化进入事业部线索池
     * @author yue.li
     */
    public void handleDeptLeads() {
        long start = System.currentTimeMillis();
        logger.info("handleDeptLeads: " + LocalDate.now());
        Record deptPoolContactSubtotal = Db.findFirst(Db.getSql("crm.leads.adminConfigByName"), CrmConstant.LEADS_DEPT_POOL_CONTACT_SUBTOTAL_SETTING);
        Record deptPoolNotTransform = Db.findFirst(Db.getSql("crm.leads.adminConfigByName"), CrmConstant.LEADS_DEPT_POOL_NOT_TRANSFORM_SETTING);
        Integer contactSubtotalDay = 0;
        Integer notTransform = 0;
        if(deptPoolContactSubtotal !=null){
            contactSubtotalDay = Integer.valueOf(deptPoolContactSubtotal.getStr("value"));
        }
        if(deptPoolNotTransform != null){
            notTransform = Integer.valueOf(deptPoolNotTransform.getStr("value"));
        }
        if(deptPoolContactSubtotal != null || deptPoolNotTransform != null){
            handlePoolLeads(contactSubtotalDay,notTransform,CrmConstant.ONE_FLAG);
        }
        long end = System.currentTimeMillis();
        logger.info("handleDeptLeads cost:" + (end - start)/1000 + "s");
        renderJson(R.ok());
    }

    /**
     * 超过N天无联系小计线索公海
     * @author yue.li
     */
    public void handlePublicLeads() {
        long start = System.currentTimeMillis();
        logger.info("handlePublicLeads: " + LocalDate.now());
        Record publicPool = Db.findFirst(Db.getSql("crm.leads.adminConfigByName"), CrmConstant.LEADS_PUBLIC_POOL_SETTING);
        Integer contactSubtotalDay = 0 ;
        if(publicPool !=null){
            contactSubtotalDay = Integer.valueOf(publicPool.getStr("value"));
            handlePoolLeads(contactSubtotalDay,null,CrmConstant.TWO_FLAG);
        }
        long end = System.currentTimeMillis();
        logger.info("handlePublicLeads cost:" + (end - start)/1000 + "s");
        renderJson(R.ok());
    }

    /**
     * 事业部线索超时4小时无领取需重复提醒
     * @author yue.li
     */
    public void handleNoticeFourHoursDeptLeads() {
        long start = System.currentTimeMillis();
        logger.info("handleNoticeFourHoursDeptLeads: " + LocalDate.now());
        sendDeptLeadsEmail(CrmEmailConstant.FOUR_HOURS);
        long end = System.currentTimeMillis();
        logger.info("handleNoticeFourHoursDeptLeads cost:" + (end - start)/1000 + "s");
        renderJson(R.ok());
    }

    /**
     * 事业部线索超时8小时无领取需重复提醒
     * @author yue.li
     */
    public void handleNoticeEightHoursDeptLeads() {
        long start = System.currentTimeMillis();
        logger.info("handleNoticeEightHoursDeptLeads: " + LocalDate.now());
        sendDeptLeadsEmail(CrmEmailConstant.EIGHT_HOURS);
        long end = System.currentTimeMillis();
        logger.info("handleNoticeEightHoursDeptLeads cost:" + (end - start)/1000 + "s");
        renderJson(R.ok());
    }

    /**
     * 事业部线索超时24小时无领取需重复提醒
     * @author yue.li
     */
    public void handleNoticeTwentyFourHoursDeptLeads() {
        long start = System.currentTimeMillis();
        logger.info("handleTwentyFourHoursDeptLeads: " + LocalDate.now());
        sendDeptLeadsEmail(CrmEmailConstant.TWENTY_FOUR_HOURS);
        long end = System.currentTimeMillis();
        logger.info("handleTwentyFourHoursDeptLeads cost:" + (end - start)/1000 + "s");
        renderJson(R.ok());
    }

    /***
     * 发送事业部线索邮件
     * @author yue.li
     * @param hours 小时
     */
    private void sendDeptLeadsEmail(int hours) {
        List<Record> recordList = Db.find(Db.getSql("crm.leads.queryDeptLeads"), CrmLeadsTransformEnum.LEADS_NOT_TRANSFORM_KEY.getTypes());
        List<CrmLeads> leadList = recordList.stream().map(item-> new CrmLeads()._setOrPut(item.getColumns())).collect(Collectors.toList());
        for(CrmLeads crmLeads : leadList) {
            long nowTime = Calendar.getInstance().getTimeInMillis();
            // 4小时发送一次
            if((nowTime < crmLeads.getCreateTime().getTime() + CrmEmailConstant.MILLI_SECONDS * CrmEmailConstant.EIGHT_HOURS) && (nowTime > crmLeads.getCreateTime().getTime() + CrmEmailConstant.MILLI_SECONDS * CrmEmailConstant.FOUR_HOURS) && Objects.isNull(crmLeads.getSendEmailTimes()) && CrmEmailConstant.FOUR_HOURS == hours) {
                crmLeadsService.sendEmail(notifyService,velocityEngine,crmLeads);
                Db.update(Db.getSql("crm.leads.setSendEmailTimes"), CrmLeadsSendTimesEnum.LEADS_SEND_EMAIL_ONE_TIMES_KEY.getTypes(),crmLeads.getLeadsId());
            }
            // 8小时发送一次
            if((nowTime < crmLeads.getCreateTime().getTime() + CrmEmailConstant.MILLI_SECONDS * CrmEmailConstant.TWENTY_FOUR_HOURS) && (nowTime >= crmLeads.getCreateTime().getTime() + CrmEmailConstant.MILLI_SECONDS * CrmEmailConstant.EIGHT_HOURS) && CrmEmailConstant.EIGHT_HOURS == hours) {
                if(CrmLeadsSendTimesEnum.LEADS_SEND_EMAIL_ONE_TIMES_KEY.getTypes().equals(crmLeads.getSendEmailTimes()) || Objects.isNull(crmLeads.getSendEmailTimes())){
                    crmLeadsService.sendEmail(notifyService,velocityEngine,crmLeads);
                    Db.update(Db.getSql("crm.leads.setSendEmailTimes"), CrmLeadsSendTimesEnum.LEADS_SEND_EMAIL_TWO_TIMES_KEY.getTypes(),crmLeads.getLeadsId());
                }
            }
            // 24小时发送一次
            if((nowTime >= crmLeads.getCreateTime().getTime() + CrmEmailConstant.MILLI_SECONDS * CrmEmailConstant.TWENTY_FOUR_HOURS) && CrmEmailConstant.TWENTY_FOUR_HOURS == hours) {
                if(CrmLeadsSendTimesEnum.LEADS_SEND_EMAIL_TWO_TIMES_KEY.getTypes().equals(crmLeads.getSendEmailTimes())) {
                    crmLeadsService.sendEmail(notifyService, velocityEngine, crmLeads);
                    Db.update(Db.getSql("crm.leads.setSendEmailTimes"), CrmLeadsSendTimesEnum.LEADS_SEND_EMAIL_THREE_TIMES_KEY.getTypes(), crmLeads.getLeadsId());
                }
            }
        }
    }

    /**
     * 处理线索池
     * @author yue.li
     * @param contactSubtotalDay  联系小计天数
     * @param notTransformDay  未转化天数
     * @param types 类型 1 事业部线索池  2 线索公海
     */
    public void handlePoolLeads(Integer contactSubtotalDay,Integer notTransformDay,String types){
        try{
            long start = System.currentTimeMillis();
            logger.info("handlePoolLeads: " + LocalDate.now());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            long nowTime = sdf.parse(sdf.format(cal.getTime())).getTime();
            List<Record> leadsList = null;
            if(types.equals(CrmConstant.ONE_FLAG)){
                leadsList = Db.find(Db.getSql("crm.leads.queryOwnLeads"),CrmConstant.ZERO_FLAG);
            }
            if(types.equals(CrmConstant.TWO_FLAG)){
                leadsList = Db.find(Db.getSql("crm.leads.queryDeptLeads"),CrmConstant.ZERO_FLAG);
            }

            if(leadsList != null && leadsList.size() >0){
                for(Record leads :leadsList){
                    logger.info(String.format("leads json %s",leads.toJson()));
                    /**超过多少天无联系小计*/
                    if( nowTime > (sdf.parse(leads.getStr("receive_time")).getTime() + 86400000 * contactSubtotalDay)){
                        Record record = Db.findFirst(Db.getSql("crm.leads.queryAdminRecordByTypesId"),CrmConstant.CRM_LEADS,leads.getStr("leads_id"));
                        if(record == null || nowTime > (sdf.parse(record.getStr("create_time")).getTime() + 86400000 * contactSubtotalDay)){
                            updateLeads(types,leads);
                            logger.info(String.format("超过多少天无联系小计更新成功 %s",leads.toJson()));
                        }
                    }
                    /**超过多少天未转化*/
                    if(notTransformDay != null){
                        if( nowTime > (sdf.parse(leads.getStr("receive_time")).getTime() + 86400000 * notTransformDay)){
                           if(leads.getStr("is_transform").equals(CrmConstant.ZERO_FLAG)){
                               updateLeads(types,leads);
                               logger.info(String.format("超过多少天未转化 %s",leads.toJson()));
                           }
                        }
                    }
                }
            }
            long end = System.currentTimeMillis();
            logger.info("handlePoolLeads cost:" + (end - start)/1000 + "s");
        }catch(Exception e){
            logger.error("handlePoolLeads{}",e);
            /**发送失败消息通知*/
            adminSendEmailService.sendErrorMessage(e,notifyService);
        }
    }

    /**
     * 更新线索
     * @author yue.li
     * @param types 类型 1 事业部线索池  2 线索公海
     * @param record 线索
     */
    public void updateLeads(String types,Record record){
        logger.info(String.format("updateLeads方法json %s",record.toJson()));
        if(types.equals(CrmConstant.ONE_FLAG)){
            String ownUserId = record.getStr("owner_user_id");
            Record ownUserRecord = Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), ownUserId);
            Integer deptId = Integer.valueOf(adminDeptService.getBusinessDepartmentByDeptId(ownUserRecord.getStr("dept_id")));
            Db.update(Db.getSql("crm.leads.putDeptPool"), deptId,record.getStr("leads_id"));
        }
        if(types.equals(CrmConstant.TWO_FLAG)){
            Db.update(Db.getSql("crm.leads.putPublicPool"), record.getStr("leads_id"));
        }
    }
}
