package com.kakarote.crm9.integration.controller;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SendMsgRecePlanController.
 *
 * @author chaokun.ding
 * @create 2019/7/24 18:00
 */
@Before(IocInterceptor.class)
public class SendMsgRecePlanController extends Controller {
    private Log logger = Log.getLog(getClass());

    @Autowired
    private NotifyService notifyService;

    @Inject
    private AdminSendEmailService adminSendEmailService;

    public void endRecePlan() {
        try{
            logger.info("endRecePlan: " + LocalDate.now());
            List<Record> records = Db.find(Db.getSql("crm.business.queryEndReceivablesPlan"));
            logger.info("到期提醒json: " + JSON.toJSONString(records));
            List<String> sendDingDingList = new ArrayList<>();

            if( records == null || records.size() ==0 ){
                renderJson(R.ok("没有需要提醒的数据！"));
                return;
            }
            for (Record r : records) {
                Record leaderRecord = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), r.get("dept_id")+"");
                String leaderId = leaderRecord.getStr("leader_id");
                //发送部门leader
                sendDingDingList.add(leaderId);
                //发送本人
                String userId = r.getStr("email").substring(0,r.getStr("email").indexOf('@'));
                sendDingDingList.add(userId);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                String msg = "时间：" + formatter.format(new Date()) + "\r\n" +
                        "商机:" + r.getStr("business_name") + "回款提醒";
                notifyService.dingTalk(msg,sendDingDingList);
                renderJson(R.ok());
            }
        }catch(Exception e){
            logger.error(String.format("endRecePlan msg:%s",BaseUtil.getExceptionStack(e)));
            /*发送失败消息通知*/
            adminSendEmailService.sendErrorMessage(e,notifyService);
            renderJson(R.error(e.getMessage()));
        }
    }
}
