package com.kakarote.crm9.integration.mq;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.theadpool.CrmThreadPool;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerExt;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.erp.crm.service.*;
import com.kakarote.crm9.integration.dto.DistributorBindContentDto;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 分销商解绑
 *
 * @author haihong.wu
 * @since 2019/7/5 9:21
 */
@Slf4j(topic = "com.kakarote.crm9.integration.controller.MqMessageCronController")
public class DistributorUnbindConsumer extends BaseDistributorConsumer {

    private String topic;

    private String tag;

    private CrmDistributorPromotionRelationService crmDistributorPromotionRelationService = Aop.get(CrmDistributorPromotionRelationService.class);

    private CrmRecordService crmRecordService = Aop.get(CrmRecordService.class);

    private CrmCustomerService crmCustomerService = Aop.get(CrmCustomerService.class);
    
    private CrmCustomerExtService crmCustomerExtService = Aop.get(CrmCustomerExtService.class);
    
    private CrmSiteMemberService crmSiteMemberService = Aop.get(CrmSiteMemberService.class);

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public List<String> tags() {
        return Collections.singletonList(tag);
    }

    @Override
    protected boolean consume(MqMsg msg) {
        DistributorBindContentDto distributorBindContentDto = JSON.toJavaObject(JSON.parseObject(msg.getContent()), DistributorBindContentDto.class);
        if (Objects.isNull(distributorBindContentDto)) {
            log.error("DistributorUnbindConsumer consume msg[id:{},msgId:{}] fail , content is null", msg.getId(), msg.getMsgId());
            return false;
        }
        Long userId = distributorBindContentDto.getAffiliateUserId();
        /* 1.删除推广关系 */
        crmDistributorPromotionRelationService.deleteByUid(userId);
        /* 2.记录操作日志(只记录已认证的数据) */
        CrmCustomer customer = crmCustomerService.queryCustomerBySiteMemberId(userId);
        if (Objects.nonNull(customer)) {
            crmRecordService.addActionRecord(null, CrmEnum.DISTRIBUTE_KEY.getTypes(), customer.getCustomerId().intValue(), "解除与原上游分销商之间的推广关系");
        }
        
        /* 3. 同步解绑信息到冗余表中 */
        CrmThreadPool.INSTANCE.getInstance().execute(() -> {
        	this.syncUnBindInfo(userId);
        });
        return true;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    
    /**
     * 同步解绑信息到冗余表中
     * @param userId
     * @return
     */
    private boolean syncUnBindInfo (Long userId) {
    	
    	// 1、同步site_member表的推广标签
    	
        // 根据网站会员id获取72crm_crm_site_member
        Record siteMember = crmSiteMemberService.getSiteMemberAllFieldBySiteMemberId(userId);
        if (Objects.nonNull(siteMember)) {
            log.info("--------->syncBindInfo Site member in db is not null: ？", siteMember);
            CrmSiteMember sm = new CrmSiteMember()._setOrPut(siteMember.getColumns());
            sm.setPromotionTag("");
            if(!sm.update()) {
            	return false;
            }
        } else {
        	log.info("--------->syncBindInfo Site member in db is null,site_member_id: ?",userId);
        }
        
    	// 2、同步客户扩展表的上游分销商的网站会员id
        // 根据网站会员id查询客户信息（包含customer_ext）
        CrmCustomerExt crmCustomerExt = crmCustomerExtService.queryCrmCustomerExtbySiteMemberId(userId);
        if (Objects.nonNull(crmCustomerExt) && Objects.nonNull(crmCustomerExt.getCustomerId())) {
            log.info("--------->syncBindInfo customer_Ext in db is not null: ？", siteMember);
            crmCustomerExt.setParentSiteMemberId(null);
            if(!crmCustomerExt.update()) {
            	return false;
            }
        }else {
        	log.info("--------->syncBindInfo customer_Ext in db is null,site_member_id: ?",userId);
        }
        
    	return true;
    }

}
