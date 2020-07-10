package com.kakarote.crm9.integration.mq;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.CrmContext;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.service.AdminConfigService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.DistributorCertifiedEnum;
import com.kakarote.crm9.erp.crm.common.DistributorPartnerTypeEnum;
import com.kakarote.crm9.erp.crm.common.PerformanceFromChannelEnum;
import com.kakarote.crm9.erp.crm.common.PerformanceObjectTypeEnum;
import com.kakarote.crm9.erp.crm.common.PerformanceTargetTypeEnum;
import com.kakarote.crm9.erp.crm.common.PromotionTagEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerExt;
import com.kakarote.crm9.erp.crm.entity.CrmDistributorPromotionRelation;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.erp.crm.service.CrmCustomerExtService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmDistributorPromotionRelationService;
import com.kakarote.crm9.erp.crm.service.CrmPerformanceService;
import com.kakarote.crm9.erp.crm.service.CrmRecordService;
import com.kakarote.crm9.erp.crm.service.CrmSiteMemberService;
import com.kakarote.crm9.erp.crm.service.bops.BopsService;
import com.kakarote.crm9.erp.crm.service.rule.customer.CustomerSplitRule;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.integration.dto.DistributorBindContentDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 分销商绑定
 *
 * @author haihong.wu
 * @since 2019/7/5 9:21
 */
@Slf4j(topic = "com.kakarote.crm9.integration.controller.MqMessageCronController")
public class DistributorBindConsumer extends BaseDistributorConsumer {

    private String topic;

    private String tag;

    private EsbConfig esbConfig;

    private CrmDistributorPromotionRelationService crmDistributorPromotionRelationService = Aop.get(CrmDistributorPromotionRelationService.class);

    private CrmRecordService crmRecordService = Aop.get(CrmRecordService.class);

    private CrmCustomerService crmCustomerService = Aop.get(CrmCustomerService.class);

    private CrmCustomerExtService crmCustomerExtService = Aop.get(CrmCustomerExtService.class);

    private CrmSiteMemberService crmSiteMemberService = Aop.get(CrmSiteMemberService.class);

    private BopsService bopsService = Aop.get(BopsService.class);

    private AdminConfigService adminConfigService = Aop.get(AdminConfigService.class);

    private CrmPerformanceService crmPerformanceService = Aop.get(CrmPerformanceService.class);

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public List<String> tags() {
        return Collections.singletonList(tag);
    }

    @Override
    protected boolean filter(JSONObject obj) {
        DistributorBindContentDto distributorBindContentDto = JSON.toJavaObject(obj, DistributorBindContentDto.class);
        if (Objects.isNull(distributorBindContentDto)) {
            return false;
        }
        DistributorPartnerTypeEnum ambassadorType = getAmbassadorType(distributorBindContentDto.getAffiliateAmbassadorType());
        DistributorPartnerTypeEnum pAmbassadorType = getAmbassadorType(distributorBindContentDto.getAmbassadorType());
        //只保留分销商消息
        return Objects.equals(DistributorPartnerTypeEnum.distributor, ambassadorType) && Objects.equals(DistributorPartnerTypeEnum.distributor, pAmbassadorType);
    }

    @Override
    protected boolean consume(MqMsg msg) {
        DistributorBindContentDto distributorBindContentDto = JSON.toJavaObject(JSON.parseObject(msg.getContent()), DistributorBindContentDto.class);
        if (Objects.isNull(distributorBindContentDto)) {
            log.error("DistributorBindConsumer consume msg[id:{},msgId:{}] fail , content is null", msg.getId(), msg.getMsgId());
            return false;
        }
        /* 1.查找现有绑定关系 */
        CrmDistributorPromotionRelation existPo = crmDistributorPromotionRelationService.findByUserId(distributorBindContentDto.getAffiliateUserId());
        /* 2.如果有就更新，没有就插入 */
        CrmDistributorPromotionRelation newPo = new CrmDistributorPromotionRelation();
        buildRelation(newPo, distributorBindContentDto);
        if (Objects.nonNull(existPo)) {
            newPo.setId(existPo.getId());
            newPo.update();
        } else {
            newPo.save();
        }
        /* 3.插入操作日志(只记录已认证的数据) */
        if (!DistributorCertifiedEnum.AUDIT.getCode().equals(newPo.getCertified())) {
            return true;
        }
        CrmCustomer customer = crmCustomerService.queryCustomerBySiteMemberId(newPo.getUid());
        if (Objects.nonNull(customer)) {
            //执行拆分规则
            CrmContext context = new CrmContext();
            CrmSiteMember siteMember = crmSiteMemberService.getBySiteMemberId(distributorBindContentDto.getAffiliateUserId());
            context.put(CrmContext.CRM_CUSTOMER, customer);
            context.put(CrmContext.SITE_MEMBER, siteMember);
            context.put(CrmContext.DISTRIBUTION_RELATION, newPo);
            CustomerSplitRule.me().execute(context);
            customer = context.getAs(CrmContext.CRM_CUSTOMER_AFTER_SPLIT);
            //保存日志
            crmRecordService.addActionRecord(null, CrmEnum.DISTRIBUTE_KEY.getTypes(), customer.getCustomerId().intValue(), "与上游分销商建立推广关系");
            //记录KPI日志
            addPerformance(customer, newPo);
        }

        /* 4.同步推广信息到冗余表中 */
        syncBindInfo(newPo);
        return true;
    }

    private void buildRelation(CrmDistributorPromotionRelation newPo, DistributorBindContentDto dto) {
        newPo.setUid(dto.getAffiliateUserId());
        newPo.setMobile(dto.getAffiliateMobile());
        newPo.setUserName(dto.getAffiliateUserName());
        newPo.setRealName(dto.getAffiliateRealName());
        newPo.setUserType(dto.getUserType());
        newPo.setCertified(RemoteDistributorAuditStatus.getCertifiedByCode(dto.getAuditStatus()));
        newPo.setPromotionTag(getPromotionTagCode(dto.getAmbTag(), Objects.isNull(dto.getAmbLevel()) ? null : (dto.getAmbLevel() + 1)));
        newPo.setPUid(dto.getUserId());
        newPo.setPLevel(dto.getAmbLevel());
    }

    /**
     * 新增kpi日志
     *
     * @param customer
     * @param relation
     */
    private void addPerformance(CrmCustomer customer, CrmDistributorPromotionRelation relation) {
        if (!PromotionTagEnum.DistributorL2.getCode().equals(relation.getPromotionTag())) {
            //只有二级分销商需要记录
            return;
        }
        /* 获取在线运营事业部部门ID */
        String onlineBusinessDeptId = adminConfigService.getConfig(CrmConstant.ONLINE_BUSINESS_DEPT_ID);
        if (StringUtils.isBlank(onlineBusinessDeptId)) {
            throw new CrmException("==分销商绑定-KPI日志记录异常-未找到在线运营事业部部门ID==");
        }
        /* 获取数字地信事业部部门ID */
        String surveyMappingDeptId = adminConfigService.getConfig(CrmConstant.SURVEY_MAPPING_DEPT_ID);
        if (StringUtils.isBlank(surveyMappingDeptId)) {
            throw new CrmException("==分销商绑定-KPI日志记录异常-未找到数字地信事业部部门ID==");
        }
        /* 查询到单数据 */
        BigDecimal orderAmount = bopsService.getCustomerOrderAmount(customer.getCustomerId(), Long.valueOf(onlineBusinessDeptId), esbConfig);
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            //到单数据0不计KPI
            return;
        }
        /* 计算KPI数值 */
        //获取倍数
        String websiteTimeStr = adminConfigService.getConfig(CrmConstant.WEBSITE_PERFORMANCE_INCLUDED);
        if (StringUtils.isBlank(websiteTimeStr)) {
            log.error("==分销商绑定-在线运营事业部KPI倍数未设置==");
            return;
        }
        //在线运营事业部KPI倍数
        BigDecimal websiteTime = BigDecimal.valueOf(Double.parseDouble(websiteTimeStr));

        String targetTimeStr = adminConfigService.getConfig(CrmConstant.TARGET_DEPT_PERFORMANCE_INCLUDED);
        if (StringUtils.isBlank(targetTimeStr)) {
            log.error("==分销商绑定-目标KPI倍数未设置==");
            return;
        }
        //目标KPI倍数
        BigDecimal targetTime = BigDecimal.valueOf(Double.parseDouble(targetTimeStr));

        //计算到年底的天数
        LocalDate now = LocalDate.now();
        LocalDate endOfYear = LocalDate.of(now.getYear(), 12, 31);
        int lastDays = endOfYear.getDayOfYear() - now.getDayOfYear();

        BigDecimal orderAmountAfterDivide = BigDecimal.valueOf(lastDays).divide(BopsService.DAYS_OF_YEAR, 6, BigDecimal.ROUND_HALF_UP).multiply(orderAmount);
        //计算部门业绩
        BigDecimal websitePerformance = orderAmountAfterDivide.multiply(websiteTime).setScale(2, BigDecimal.ROUND_HALF_UP);
        //计算BD业绩
        BigDecimal targetPerformance = orderAmountAfterDivide.multiply(targetTime).setScale(2, BigDecimal.ROUND_HALF_UP);
        String batchId = IdUtil.fastSimpleUUID();
        /* 在线运营事业部扣减 */
        crmPerformanceService.addPerformance(batchId, Long.valueOf(onlineBusinessDeptId), PerformanceObjectTypeEnum.DEPARTMENT.getCode(), null, websitePerformance.negate(), PerformanceFromChannelEnum.DISTRIBUTOR_BIND, Long.valueOf(surveyMappingDeptId), PerformanceTargetTypeEnum.CUSTOMER, customer.getCustomerId(), PerformanceFromChannelEnum.DISTRIBUTOR_BIND.getDesc(), relation.getGmtCreate());
        /* 数字地信事业部增加 */
        crmPerformanceService.addPerformance(batchId, Long.valueOf(surveyMappingDeptId), PerformanceObjectTypeEnum.DEPARTMENT.getCode(), null, targetPerformance, PerformanceFromChannelEnum.DISTRIBUTOR_BIND, Long.valueOf(onlineBusinessDeptId), PerformanceTargetTypeEnum.CUSTOMER, customer.getCustomerId(), PerformanceFromChannelEnum.DISTRIBUTOR_BIND.getDesc(), relation.getGmtCreate());
    }

    /**
     * 同步推广信息到冗余表中
     *
     * @param newPo
     * @return
     */
    private void syncBindInfo(CrmDistributorPromotionRelation newPo) {
        if (Objects.isNull(newPo)) {
            // 没有推广关系，不做处理
            return;
        }
        // 1、同步site_member表的推广标签
        // 下游网站会员id
        Long siteMemberId = newPo.getUid();
        // 上游分销商id
        Long parentSiteMemberId = newPo.getPUid();

        // 根据网站会员id获取72crm_crm_site_member
        Record siteMember = crmSiteMemberService.getSiteMemberAllFieldBySiteMemberId(siteMemberId);
        if (Objects.nonNull(siteMember)) {
            log.info("--------->syncBindInfo Site member in db is not null: {}", siteMember);
            CrmSiteMember sm = new CrmSiteMember()._setOrPut(siteMember.getColumns());
            sm.setPromotionTag(newPo.getPromotionTag());
            if (!sm.update()) {
                return;
            }
        } else {
            log.info("--------->syncBindInfo Site member in db is null,site_member_id: {}", siteMemberId);
        }

        // 2、同步客户扩展表的上游分销商的网站会员id
        // 根据网站会员id查询客户信息（包含customer_ext）
        CrmCustomerExt crmCustomerExt = crmCustomerExtService.queryCrmCustomerExtbySiteMemberId(siteMemberId);
        if (Objects.nonNull(crmCustomerExt) && Objects.nonNull(crmCustomerExt.getCustomerId())) {
            log.info("--------->syncBindInfo customer_Ext in db is not null: {}", siteMember);
            crmCustomerExt.setParentSiteMemberId(parentSiteMemberId);
            crmCustomerExt.update();
        } else if (Objects.nonNull(crmCustomerExt) && Objects.nonNull(crmCustomerExt.getMainCustomerId())) {
            log.info("--------->syncBindInfo customer_Ext in db is null,site_member_id: {}", siteMemberId);
            crmCustomerExt.setCustomerId(crmCustomerExt.getMainCustomerId().intValue());
            crmCustomerExt.setParentSiteMemberId(parentSiteMemberId);
            crmCustomerExt.save();
        } else {
            log.info("--------->syncBindInfo customer in db is null,site_member_id: {}", siteMemberId);
        }
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setEsbConfig(EsbConfig esbConfig) {
        this.esbConfig = esbConfig;
    }
}
