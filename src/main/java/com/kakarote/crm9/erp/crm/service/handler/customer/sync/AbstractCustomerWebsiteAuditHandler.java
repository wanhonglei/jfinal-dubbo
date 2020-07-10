package com.kakarote.crm9.erp.crm.service.handler.customer.sync;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.kakarote.crm9.common.CrmContext;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmDistributorPromotionRelation;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.integration.entity.SiteMember;
import com.kakarote.crm9.integration.entity.SpecialCustomerAllocateRuleDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/2 3:04 下午
 */
@Slf4j
public abstract class AbstractCustomerWebsiteAuditHandler extends BaseSyncHandler {

    protected static final String SPECIAL_CUSTOMER_ALLOCATE_RULE = JfinalConfig.crmProp.get("special.customer.allocate.rule");

    /**
     * 特殊渠道分发
     *
     * @param param
     * @param crmContext
     */
    protected void handleChannel(SiteMember param, CrmContext crmContext) {
        if (StringUtils.isNotBlank(param.getChannel())) {
            CrmCustomer crmCustomer = crmContext.getAs(CrmContext.CRM_CUSTOMER);
            Integer newOwnerUserId = getMemberChannelOwnerUserId(param.getChannel());
            if (Objects.nonNull(newOwnerUserId)) {
                crmCustomerService.memberChannelUpdateOwnerUserId(crmCustomer.getCustomerId(), newOwnerUserId);
                log.info("==官网认证-特殊渠道分发(channel:{},uid:{},cid:{},ownerUserId:{})==", param.getChannel(), param.getId(), crmCustomer.getCustomerId(), newOwnerUserId);
            }
        }
    }

    /**
     * 更新分销商推广关系数据
     */
    protected void handleDistributionRelation(CrmContext crmContext, Integer userType, Integer certified, String realName) {
        CrmDistributorPromotionRelation relation = crmContext.getAs(CrmContext.DISTRIBUTION_RELATION);
        if (Objects.isNull(relation)) {
            return;
        }
        CrmCustomer crmCustomer = crmContext.getAs(CrmContext.CRM_CUSTOMER);
        if (Objects.isNull(crmCustomer)) {
            return;
        }
        relation.setUserType(userType);
        relation.setCertified(certified);
        relation.setRealName(realName);
        relation.update();
        //插入日志
        crmRecordService.addActionRecord(null, CrmEnum.DISTRIBUTE_KEY.getTypes(), crmCustomer.getCustomerId().intValue(), relation.getGmtCreate(), "与上游分销商建立推广关系");
    }

    /**
     * 通过会员渠道查询默认分派的bd
     *
     * @param memberChannel
     * @return
     */
    private Integer getMemberChannelOwnerUserId(String memberChannel) {
        if (StringUtils.isBlank(memberChannel)) {
            return null;
        }
        Map<String, Long> memberChannelRuleMap = getChannelRuleMap();
        Long ownerUserId = memberChannelRuleMap.get(memberChannel);
        if (Objects.nonNull(ownerUserId)) {
            return ownerUserId.intValue();
        }
        return null;
    }

    /**
     * 获取会员注册渠道默认bd配置规则
     * tmall:2804
     *
     * @return
     */
    private Map<String, Long> getChannelRuleMap() {
        //查询会员注册渠道默认bd分派规则，根据规则分派客户
        List<SpecialCustomerAllocateRuleDto> allocateRuleDtos = JSONObject.parseArray(SPECIAL_CUSTOMER_ALLOCATE_RULE, SpecialCustomerAllocateRuleDto.class);
        Map<String, Long> ruleMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(allocateRuleDtos)) {
            for (SpecialCustomerAllocateRuleDto ruleDto : allocateRuleDtos) {
                String memberChannel = ruleDto.getMemberChannel();
                Long ownerUserId = ruleDto.getOwnerUserId();
                if (Objects.nonNull(ownerUserId)) {
                    AdminUser adminUser = adminUserService.getAdminUserByUserId(ownerUserId);
                    if (Objects.nonNull(adminUser)) {
                        ruleMap.put(memberChannel, ownerUserId);
                    }
                }
            }
        }
        return ruleMap;
    }

    /**
     * 尝试更新客户
     * 如果客户下面没有绑定其他网站会员，则更新
     *
     * @param crmCustomer
     * @param siteMember
     */
    protected void tryUpdateCustomer(CrmCustomer crmCustomer, SiteMember siteMember) {
        List<CrmSiteMember> existSiteMemberList = crmSiteMemberService.queryByCustomerId(crmCustomer.getCustomerId());
        boolean needUpdate = true;
        if (CollectionUtils.isNotEmpty(existSiteMemberList)) {
            for (CrmSiteMember crmSiteMember : existSiteMemberList) {
                if (crmSiteMember.getSiteMemberId().equals(siteMember.getId())) {
                    continue;
                }
                needUpdate = false;
                break;
            }
        }
        if (needUpdate) {
            //组装客户数据
            assembleCrmCustomer(crmCustomer, siteMember);
            //更新
            crmCustomer.update();
        }
    }
}
