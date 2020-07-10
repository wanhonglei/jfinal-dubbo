package com.kakarote.crm9.integration.mq;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmDistributorPromotionRelation;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.erp.crm.service.CrmMqMessageService;
import com.kakarote.crm9.erp.crm.service.bops.BopsService;
import com.kakarote.crm9.integration.dto.DistributorBindContentDto;
import com.kakarote.crm9.integration.service.MqDistributorAuditCronService;
import com.kakarote.crm9.utils.CrmProps;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/27 10:23 上午
 */
public class DistributorConsumerTest extends BaseTest {

    private ThirdDistributorAuditConsumer thirdDistributorAuditConsumer = Aop.get(ThirdDistributorAuditConsumer.class);
    private MqDistributorAuditCronService mqDistributorAuditCronService = Aop.get(MqDistributorAuditCronService.class);
    private DistributorBindConsumer distributorBindConsumer = Aop.get(DistributorBindConsumer.class);
    private DistributorUnbindConsumer distributorUnbindConsumer = Aop.get(DistributorUnbindConsumer.class);
    private DistributorFreezeConsumer distributorFreezeConsumer = Aop.get(DistributorFreezeConsumer.class);
    private CrmMqMessageService crmMqMessageService = Aop.get(CrmMqMessageService.class);

    @Test
    public void distributorAudit() {
        CrmProps props = CrmProps.getInstance();
        String topic = props.get("backend.distributor.audit.topic");
        String tag = props.get("backend.distributor.audit.success.tag");
        thirdDistributorAuditConsumer.setTopic(topic);
        thirdDistributorAuditConsumer.setAuditTag(tag);
        thirdDistributorAuditConsumer.execute(buildMsgExt(topic, tag),
                "{\"id\": 168995461, \"email\": \"445335@qq.com\", \"level\": \"platinum\", \"bdDept\": \"48284112\", \"mobile\": \"14499983000\", \"linkMan\": \"qweqwe\", \"areaCode\": \"150000\", \"areaName\": \"内蒙古自治区\", \"realName\": \"cxcxa\", \"userName\": \"cscsx\", \"userType\": 1, \"applyTime\": 1587889274000, \"gmtCreate\": 1587889274000, \"bdUserName\": \"Person\", \"bdUserEmail\": \"1957345722-1907849355@wz-inc.com\", \"contactEmail\": \"\", \"addressDetail\": \"测试测试\", \"contactNumber\": \"14499983000\", \"isDistributor\": 1, \"operateStatus\": 1, \"contactAddress\": \"天津市市辖区河西区 测试测试\", \"addressAreaCode\": \"120103\", \"addressAreaName\": \"天津市市辖区河西区\", \"auditSuccessTime\": 1587889551000, \"distributorStatus\": 3, \"businessLicenceEnd\": 1682524799000, \"businessLicenceNum\": \"444433333266666234\", \"businessLicenceBegin\": 1587830400000, \"businessLicenceImgUrl\": \"https://dev-venus-pri.oss-cn-beijing.aliyuncs.com/license/878/537/1405e277a1be74f3e8c60b879c3ce061.png\"}"
        );
        mqDistributorAuditCronService.handleMsg();
    }

    @Test
    public void distributorBind() {
        Db.tx(() -> {
            CrmProps props = CrmProps.getInstance();
            String topic = props.get("mq.distributor.bind.topic");
            String tag = props.get("mq.distributor.bind.tag");
            distributorBindConsumer.setTopic(topic);
            distributorBindConsumer.setTag(tag);
            distributorBindConsumer.execute(null, "{}");
            distributorBindConsumer.execute(buildMsgExt(topic, tag), null);
            distributorBindConsumer.execute(buildMsgExt(topic, tag), 1);
            distributorBindConsumer.execute(buildMsgExt(topic, tag), "[]");
            List<MqMsg> message = crmMqMessageService.findUnConsumedMessageByTopicAndTags(topic, Collections.singletonList(tag));
            distributorBindConsumer.consume(message);
            return false;
        });
    }

    @Test
    public void distributorBind1() {
        Db.tx(() -> {
            MqMsg msg = new MqMsg();
            msg.setId(-1L);
            msg.setMsgId("-1008611L");
            DistributorBindContentDto dto = JSONObject.parseObject("{\n" +
                    "  \"affiliateAmbassadorType\": \"dr_offline\",\n" +
                    "  \"affiliateMobile\": \"14499983000\",\n" +
                    "  \"affiliateRealName\": \"cxcxa\",\n" +
                    "  \"affiliateUserId\": 9999988,\n" +
                    "  \"ambLevel\": 1,\n" +
                    "  \"ambTag\": \"level\",\n" +
                    "  \"ambassadorType\": \"dr_offline\",\n" +
                    "  \"auditStatus\": 3,\n" +
                    "  \"bindType\": \"bind\",\n" +
                    "  \"userId\": 9999989,\n" +
                    "  \"userType\": 1\n" +
                    "}", DistributorBindContentDto.class);
            msg.setContent(JSON.toJSONString(dto));
            distributorBindConsumer.consume(msg);
            return false;
        });
    }

    @Test
    public void distributorBind2() {
        Db.tx(() -> {
            String customerNo = "UUUU";
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setCustomerName("UnitTests1");
            crmCustomer.setOwnerUserId(1992);
            crmCustomer.setOwnerTime(new Date());
            crmCustomer.setOwnerUserName("wuhh");
            crmCustomer.setCustomerNo(customerNo);
            crmCustomer.setBatchId("--11");
            crmCustomer.save();
            CrmSiteMember siteMember1 = new CrmSiteMember();
            siteMember1.setSiteMemberId(9999988L);
            siteMember1.setRealName("unitTest");
            siteMember1.setCustId(customerNo);
            siteMember1.save();
            MqMsg msg = new MqMsg();
            msg.setId(-1L);
            msg.setMsgId("-1008611L");
            DistributorBindContentDto dto = JSONObject.parseObject("{\n" +
                    "  \"affiliateAmbassadorType\": \"dr_offline\",\n" +
                    "  \"affiliateMobile\": \"14499983000\",\n" +
                    "  \"affiliateRealName\": \"cxcxa\",\n" +
                    "  \"affiliateUserId\": 9999988,\n" +
                    "  \"ambLevel\": 1,\n" +
                    "  \"ambTag\": \"level\",\n" +
                    "  \"ambassadorType\": \"dr_offline\",\n" +
                    "  \"auditStatus\": 3,\n" +
                    "  \"bindType\": \"bind\",\n" +
                    "  \"userId\": 9999989,\n" +
                    "  \"userType\": 1\n" +
                    "}", DistributorBindContentDto.class);
            msg.setContent(JSON.toJSONString(dto));
            try {
                mockBopsService(BigDecimal.valueOf(100));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            distributorBindConsumer.consume(msg);
            CrmSiteMember siteMember3 = CrmSiteMember.dao.findFirst("select * from 72crm_crm_site_member where site_member_id = ?", 9999988L);
            Assert.assertNotNull(siteMember3.getPromotionTag());
            CrmCustomer crmCustomer1 = CrmCustomer.dao.findFirst("select * from 72crm_crm_customer where customer_no = ?", siteMember3.getCustId());
            Assert.assertEquals(crmCustomer.getCustomerId(), crmCustomer1.getCustomerId());
            CrmDistributorPromotionRelation relation = CrmDistributorPromotionRelation.dao.findFirst("select * from 72crm_crm_distributor_promotion_relation where uid = ?", 9999988L);
            Assert.assertNotNull(relation);
            return false;
        });
    }

    @Test
    public void distributorBind3() {
        Db.tx(() -> {
            String customerNo = "UUUU";
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setCustomerName("UnitTests1");
            crmCustomer.setOwnerUserId(1992);
            crmCustomer.setOwnerTime(new Date());
            crmCustomer.setOwnerUserName("wuhh");
            crmCustomer.setCustomerNo(customerNo);
            crmCustomer.setBatchId("--11");
            crmCustomer.save();
            CrmSiteMember siteMember1 = new CrmSiteMember();
            siteMember1.setSiteMemberId(9999988L);
            siteMember1.setRealName("unitTest");
            siteMember1.setCustId(customerNo);
            siteMember1.save();
            CrmSiteMember siteMember2 = new CrmSiteMember();
            siteMember2.setSiteMemberId(9999988L - 4);
            siteMember2.setRealName("unitTest");
            siteMember2.setCustId(customerNo);
            siteMember2.save();
            MqMsg msg = new MqMsg();
            msg.setId(-1L);
            msg.setMsgId("-1008611L");
            DistributorBindContentDto dto = JSONObject.parseObject("{\n" +
                    "  \"affiliateAmbassadorType\": \"dr_offline\",\n" +
                    "  \"affiliateMobile\": \"14499983000\",\n" +
                    "  \"affiliateRealName\": \"cxcxa\",\n" +
                    "  \"affiliateUserId\": 9999988,\n" +
                    "  \"ambLevel\": 1,\n" +
                    "  \"ambTag\": \"level\",\n" +
                    "  \"ambassadorType\": \"dr_offline\",\n" +
                    "  \"auditStatus\": 3,\n" +
                    "  \"bindType\": \"bind\",\n" +
                    "  \"userId\": 9999989,\n" +
                    "  \"userType\": 1\n" +
                    "}", DistributorBindContentDto.class);
            msg.setContent(JSON.toJSONString(dto));
            try {
                mockBopsService(BigDecimal.valueOf(102));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            distributorBindConsumer.consume(msg);
            CrmSiteMember siteMember3 = CrmSiteMember.dao.findFirst("select * from 72crm_crm_site_member where site_member_id = ?", 9999988L);
            Assert.assertNotNull(siteMember3.getPromotionTag());
            CrmCustomer crmCustomer1 = CrmCustomer.dao.findFirst("select * from 72crm_crm_customer where customer_no = ?", siteMember3.getCustId());
            Assert.assertEquals(crmCustomer.getCustomerId(), crmCustomer1.getCustomerId());
            CrmDistributorPromotionRelation relation = CrmDistributorPromotionRelation.dao.findFirst("select * from 72crm_crm_distributor_promotion_relation where uid = ?", 9999988L);
            Assert.assertNotNull(relation);
            return false;
        });
    }

    private void mockBopsService(BigDecimal amount) throws NoSuchFieldException, IllegalAccessException {
        BopsService bopsService = Mockito.mock(BopsService.class);
        Mockito.when(bopsService.getCustomerOrderAmount(Mockito.anyLong(), Mockito.anyLong(), Mockito.any())).thenReturn(amount);
        injectField(DistributorBindConsumer.class, distributorBindConsumer, "bopsService", bopsService);
    }

    @Test
    public void distributorBind4() {
        Db.tx(() -> {
            String customerNo = "UUUU";
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setCustomerName("UnitTests1");
            crmCustomer.setCustomerNo(customerNo);
            crmCustomer.setCustomerType("153");
            crmCustomer.setBatchId("--11");
            crmCustomer.setDeptId(193);
            crmCustomer.save();
            CrmSiteMember siteMember1 = new CrmSiteMember();
            siteMember1.setSiteMemberId(9999988L);
            siteMember1.setRealName("unitTest");
            siteMember1.setCustId(customerNo);
            siteMember1.save();
            CrmSiteMember siteMember2 = new CrmSiteMember();
            siteMember2.setSiteMemberId(9999988L - 4);
            siteMember2.setRealName("unitTest");
            siteMember2.setCustId(customerNo);
            siteMember2.save();
            MqMsg msg = new MqMsg();
            msg.setId(-1L);
            msg.setMsgId("-1008611L");
            DistributorBindContentDto dto = JSONObject.parseObject("{\n" +
                    "  \"affiliateAmbassadorType\": \"dr_offline\",\n" +
                    "  \"affiliateMobile\": \"14499983000\",\n" +
                    "  \"affiliateRealName\": \"cxcxa\",\n" +
                    "  \"affiliateUserId\": 9999988,\n" +
                    "  \"ambLevel\": 1,\n" +
                    "  \"ambTag\": \"level\",\n" +
                    "  \"ambassadorType\": \"dr_offline\",\n" +
                    "  \"auditStatus\": 3,\n" +
                    "  \"bindType\": \"bind\",\n" +
                    "  \"userId\": 9999989,\n" +
                    "  \"userType\": 1\n" +
                    "}", DistributorBindContentDto.class);
            msg.setContent(JSON.toJSONString(dto));
            try {
                mockBopsService(BigDecimal.valueOf(105));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            distributorBindConsumer.consume(msg);
            CrmSiteMember siteMember3 = CrmSiteMember.dao.findFirst("select * from 72crm_crm_site_member where site_member_id = ?", 9999988L);
            Assert.assertNotNull(siteMember3.getPromotionTag());
            CrmCustomer crmCustomer1 = CrmCustomer.dao.findFirst("select * from 72crm_crm_customer where customer_no = ?", siteMember3.getCustId());
            Assert.assertNotEquals(crmCustomer.getCustomerId(), crmCustomer1.getCustomerId());
            CrmDistributorPromotionRelation relation = CrmDistributorPromotionRelation.dao.findFirst("select * from 72crm_crm_distributor_promotion_relation where uid = ?", 9999988L);
            Assert.assertNotNull(relation);
            return false;
        });
    }

    @Test
    public void distributorUnBind() {
        CrmProps props = CrmProps.getInstance();
        String topic = props.get("mq.distributor.unbind.topic");
        String tag = props.get("mq.distributor.unbind.tag");
        distributorUnbindConsumer.setTopic(topic);
        distributorUnbindConsumer.setTag(tag);
        List<MqMsg> message = crmMqMessageService.findUnConsumedMessageByTopicAndTags(topic, Collections.singletonList(tag));
        distributorUnbindConsumer.consume(message);
    }

    @Test
    public void distributorFreeze() {
        CrmProps props = CrmProps.getInstance();
        String topic = props.get("backend.distributor.audit.topic");
        String tag = props.get("mq.distributor.freeze.tag");
        distributorFreezeConsumer.setTopic(topic);
        distributorFreezeConsumer.setTag(tag);
    }

    private MessageExt buildMsgExt(String topic, String tag) {
        return buildMsgExt(topic, tag, null);
    }

    private MessageExt buildMsgExt(String topic, String tag, String msgId) {
        MessageExt messageExt = new MessageExt();
        messageExt.setMsgId(msgId == null ? IdUtil.fastSimpleUUID() : msgId);
        messageExt.setTopic(topic);
        messageExt.setTags(tag);
        return messageExt;
    }
}
