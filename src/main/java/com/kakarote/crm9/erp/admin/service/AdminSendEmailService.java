package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Inject;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.erp.admin.entity.SendEmailEntity;
import com.kakarote.crm9.utils.BaseUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author honglei.wan
 */
public class AdminSendEmailService {

    @Inject
    private AdminDeptService adminDeptService;

    private Log logger = Log.getLog(getClass());

    /**
     * 发送消息内容
     * @author liyue
     * @param sendEmailEntity 发送实体
     *
     */
    public void sendEmail(SendEmailEntity sendEmailEntity){
        logger.info(String.format("sendEmail方法实体 %s",sendEmailEntity.toString()));
        List<String> sendEmailList = new ArrayList<String>();

        /**设置抄送人*/
        if(sendEmailEntity.getCopyUserList() != null){
            for(String copyUserId : sendEmailEntity.getCopyUserList()){
                sendEmailList.add(getEmail(copyUserId));
            }
        }
        /**设置负责人*/
        if(sendEmailEntity.getOwnerUserId() != null && !"".equals(sendEmailEntity.getOwnerUserId())){
            sendEmailList.add(getEmail(sendEmailEntity.getOwnerUserId()));
        }
        /**设置负责人leader*/
        if(sendEmailEntity.isSendLeader()){
            Record ownUserRecord = Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), sendEmailEntity.getOwnerUserId());
            Integer deptId = Integer.valueOf(adminDeptService.getBusinessDepartmentByDeptId(ownUserRecord.getStr("dept_id")));
            Record leaderRecord = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), deptId);
            String leaderId = leaderRecord.getStr("leader_id");
            sendEmailList.add(getEmail(leaderId));
        }
        /**发送邮件*/
        sendEmailEntity.getNotifyService().email(sendEmailEntity.getTitle(),sendEmailEntity.getContent(),sendEmailList);
    }

    /***
     * 获取人员对应的邮箱
     * @param userId 人员ID
     */
    public String getEmail(String userId){
        Record emailRecord = Db.findFirst(Db.getSql("admin.user.getDeptInfoByUserName"), userId);
        return emailRecord.getStr("email");
    }

    /***
     * 发送异常消息通知
     * @author liyue
     * @param e 异常信息
     */
    public void sendErrorMessage(Exception e, NotifyService notifyService){
        String emailList = JfinalConfig.crmProp.get("mq.warning.email.tolist");
        String title = JfinalConfig.crmProp.get("mq.warning.email.topic");
        String[] userIds = emailList.split(",");
        String err = BaseUtil.getExceptionStack(e);
        notifyService.email(title,err, Arrays.asList(userIds));
    }

}
