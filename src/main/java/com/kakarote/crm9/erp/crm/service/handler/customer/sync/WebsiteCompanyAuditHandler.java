package com.kakarote.crm9.erp.crm.service.handler.customer.sync;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.common.CrmContext;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.DistributorCertifiedEnum;
import com.kakarote.crm9.erp.crm.common.SiteMemberUserTypeEnum;
import com.kakarote.crm9.erp.crm.common.customer.FromSourceEnum;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmDistributorPromotionRelation;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.integration.common.ThirdCustomerOriginEnum;
import com.kakarote.crm9.integration.entity.SiteMember;
import com.kakarote.crm9.utils.R;
import com.qxwz.venus.common.utils.IdCardEncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 网站会员认证-企业认证
 *
 * @Author: haihong.wu
 * @Date: 2020/6/1 10:15 上午
 */
@Slf4j
public class WebsiteCompanyAuditHandler extends AbstractCustomerWebsiteAuditHandler {

    /**
     * 1.1、该网站会员账号的真实名称（公司名称）与某crm客户名称相同的（客户名称存在校验位时，如：千寻位置网络有限公司(3467)，去掉校验位(3467)后，再跟网站会员账号比较），且该网站会员账号的手机号码与某crm客户的联系人手机号码一致的
     * <p>
     * 1.1.1、该网站会员账号为分销伙伴终端用户或二级分销伙伴：生成新的crm客户，生成的crm客户与该网站会员账号关联
     * <p>
     * 1.1.2、该网站会员账号为分销商：
     * <p>
     * 1）、匹配到的crm客户中，关联网站会员账号（uid）中无分销身份（分销身份包含：分销商、二级分销伙伴、分销伙伴终端用户）的uid时，将网站会员账号合并到crm客户上（建立关联关系）
     * <p>
     * 2）、匹配到的crm客户中，关联网站会员账号（uid）中有分销身份（分销身份包含：分销商、二级分销伙伴、分销伙伴终端用户）的uid时，生成新的crm客户，生成的crm客户与该网站会员账号关联
     * <p>
     * 1.1.3、该网站会员账号为无分销身份：
     * <p>
     * 1）、匹配到的crm客户中，关联网站会员账号（uid）中无分销身份的uid时，将网站会员账号合并到crm客户上（建立关联关系）
     * <p>
     * 2）、匹配到的crm客户中，关联网站会员账号（uid）中有分销商的uid时，将网站会员账号合并到crm客户上（建立关联关系）
     * <p>
     * 3）、匹配到的crm客户中，关联网站会员账号（uid）中有二级分销伙伴或分销伙伴终端用户的uid时，生成新的crm客户，生成的crm客户与该网站会员账号关联
     * <p>
     * 1.2、不满足条件：该网站会员账号的真实名称（公司名称）与某crm客户名称相同的（客户名称存在校验位时，如：千寻位置网络有限公司(3467)，去掉校验位(3467)后，再跟网站会员账号比较），且该网站会员账号的手机号码与某crm客户的联系人手机号码一致的
     * <p>
     * 1）、生成新的crm客户，生成的crm客户与该网站会员账号关联
     *
     * @param obj
     */
    @Override
    public void handle(Object obj) {
        log.info("==官网认证-企业认证-开始({})==", JsonKit.toJson(obj));
        if (!(obj instanceof SiteMember)) {
            throw new CrmException("未知参数类型:" + obj.getClass().getSimpleName());
        }
        SiteMember param = (SiteMember) obj;
        /* 根据网站会员ID查询现存网站会员数据 */
        CrmSiteMember existSiteMember = crmSiteMemberService.getBySiteMemberId(param.getId());
        if (Objects.nonNull(existSiteMember)) {
            //如果现存网站会员数据直接返回
            log.info("==官网认证-企业认证-已存在ID相同的SiteMember数据[Context:{}]==", JsonKit.toJson(obj));
            return;
        }
        Db.tx(() -> {
            /* 上下文对象 */
            CrmContext crmContext = new CrmContext();
            /* 查询推广关系 */
            queryDistributorRelation(param.getId(), crmContext);
            /* 客户表逻辑 */
            handleCustomer(param, crmContext);
            /* 网站会员表逻辑 */
            handleSiteMember(param, crmContext);
            /* 联系人逻辑 */
            handleContacts(param, crmContext);
            /* 附加逻辑 */
            /* 1.特殊渠道分发 */
            handleChannel(param, crmContext);
            /* 2.更新推广关系 */
            handleDistributionRelation(param, crmContext);
            /* 99.冗余上游分销商 */
            redundancyParentSiteMemberId(param.getId(), crmContext);
            return true;
        });
    }

    /**
     * 客户表逻辑
     * 1、合并拆分判断
     * 2、组装数据
     * 3、插入数据
     *
     * @param param
     * @param crmContext
     */
    private void handleCustomer(SiteMember param, CrmContext crmContext) {
        //网站会员名称
        String realName = param.getRealName();
        CrmCustomer crmCustomer = null;
        /* 保留名称符合规则，联系号码相同的客户 */
        //左模糊查询
        List<CrmCustomer> customerByRealNameLeftLike = crmCustomerService.getByRealNameLeftLike(realName);
        //匹配网站会员名称[+(4位随机数)]
        Pattern pattern = Pattern.compile("^" + realName + "$|^" + realName + "\\(\\d{4}\\)$");
        //名称过滤后的List
        List<CrmCustomer> afterFilterName = customerByRealNameLeftLike.stream().filter(item -> pattern.matcher(item.getCustomerName()).matches()).collect(Collectors.toList());
        //联系人过滤后的List
        List<CrmCustomer> afterFilterContacts = afterFilterName.stream().filter(item -> crmContactsService.exists(item.getCustomerId(), param.getMobile())).collect(Collectors.toList());
        //存在匹配的数据并且无分销身份
        if (CollectionUtils.isNotEmpty(afterFilterContacts) && Objects.isNull(crmContext.get(CrmContext.DISTRIBUTION_RELATION))) {
            if (afterFilterContacts.size() > 1) {
                log.info("==官网认证-企业认证-存在匹配数据多条-param:{}==", JsonKit.toJson(param));
            }
            //网站认证的客户肯定不是分销商，所以只需要判断推广标签即可
            //无分销身份，判断匹配到的CRM客户对象
            CrmCustomer existCustomer = afterFilterContacts.get(0);
            //获取匹配到的CRM客户下的所有网站会员数据
            List<CrmSiteMember> existCustomerRelateSiteMembers = crmSiteMemberService.queryByCustomerId(existCustomer.getCustomerId());
            boolean doMerge = false;
            if (CollectionUtils.isEmpty(existCustomerRelateSiteMembers)) {
                //如果客户下没有网站会员账号，合并
                doMerge = true;
            } else {
                for (CrmSiteMember existCustomerRelateSiteMember : existCustomerRelateSiteMembers) {
                    if (Objects.equals(existCustomerRelateSiteMember.getSiteMemberId(), param.getId())) {
                        continue;
                    }
                    CrmDistributorPromotionRelation existSiteMemberRelation = crmDistributorPromotionRelationService.findByUserId(existCustomerRelateSiteMember.getSiteMemberId());
                    if (Objects.nonNull(existSiteMemberRelation)) {
                        //如果已存在的网站会员帐户存在,则不合并
                        doMerge = false;
                        break;
                    }
                    doMerge = true;
                }
            }
            if (doMerge) {
                //合并
                crmCustomer = existCustomer;
                //尝试更新老的customer
                tryUpdateCustomer(crmCustomer, param);
                //插入合并日志
                crmRecordService.addActionRecord(null, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmCustomer.getCustomerId().intValue(), String.format("合并网站会员账号(网站用户ID:%s)", param.getId()));
                log.info("==官网认证-企业认证-合并客户(uid:{},cid:{})==", param.getId(), crmCustomer.getCustomerId());
            }
        }
        //其余情况，生成新的crm客户
        if (Objects.isNull(crmCustomer)) {
            crmCustomer = assembleCrmCustomer(new CrmCustomer(), param);
            if (CollectionUtils.isNotEmpty(afterFilterName)) {
                // 当客户在crm中已经存在，则在当前客户姓名之后加上（四位随机数）·
                crmCustomer.setCustomerName(param.getRealName() + "(" + RandomUtil.randomNumbers(4) + ")");
                crmCustomer.setOriginalCustomerName(param.getRealName());
            }
            crmCustomer.setFromSource(FromSourceEnum.FROM_WEBSITE.getCode());
            //插入客户数据
            R addResult = crmCustomerService.addOrUpdate(new JSONObject().fluentPut("entity", crmCustomer), null);
            if (!addResult.isSuccess()) {
                throw new CrmException("==企业客户认证失败-插入客户失败,原因:" + addResult.get("msg") + "==");
            }
            crmCustomer.setCustomerId((Long) ((Map) addResult.get("data")).get("customer_id"));
            crmCustomer.setCustomerNo((String) ((Map) addResult.get("data")).get("customer_no"));

            //插入客户插入日志
            crmRecordService.addRecord(crmCustomer.getCustomerId().intValue(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), null);

            log.info("==官网认证-企业认证-插入客户(uid:{},customer:{})==", param.getId(), JsonKit.toJson(crmCustomer));
        }
        crmContext.put(CrmContext.CRM_CUSTOMER, crmCustomer);
    }

    /**
     * 网站会员逻辑
     *
     * @param param
     * @param crmContext
     */
    private void handleSiteMember(SiteMember param, CrmContext crmContext) {
        CrmCustomer crmCustomer = crmContext.getAs(CrmContext.CRM_CUSTOMER);
        CrmDistributorPromotionRelation relation = crmContext.getAs(CrmContext.DISTRIBUTION_RELATION);
        /*组装数据*/
        CrmSiteMember siteMember = assembleCrmSiteMember(new CrmSiteMember(), param);
        siteMember.setDataOrigin(ThirdCustomerOriginEnum.SITE_MEMBER.getCode());
        siteMember.setCustId(crmCustomer.getCustomerNo());
        //推广标签
        if (Objects.nonNull(relation)) {
            siteMember.setPromotionTag(relation.getPromotionTag());
        }
        /* 保存数据 */
        siteMember.save();

        log.info("==官网认证-企业认证-插入网站会员数据({})==", JsonKit.toJson(siteMember));
    }

    /**
     * 联系人逻辑
     *
     * @param param
     * @param crmContext
     */
    private void handleContacts(SiteMember param, CrmContext crmContext) {
        CrmCustomer crmCustomer = crmContext.getAs(CrmContext.CRM_CUSTOMER);
        /* 判断是否存在现存联系人 */
        if (!crmContactsService.exists(crmCustomer.getCustomerId(), param.getMobile())) {
            //不存在则插入
            CrmContacts contacts = new CrmContacts();
            contacts.setCustomerId(crmCustomer.getCustomerId().intValue());
            contacts.setName(param.getLinkMan());
            contacts.setMobile(StringUtils.isNotEmpty(param.getMobile()) ? param.getMobile() : param.getContactNumber());
            contacts.setEmail(param.getEmail());
            contacts.setAddress(param.getContactAddress());
            contacts.setCreateTime(new Date());
            contacts.setBatchId("");

            JSONObject obj = new JSONObject();
            obj.put("entity", contacts);
            R result = crmContactsService.addOrUpdate(obj, null);
            if (result.isSuccess()) {
                log.info("==官网认证-企业认证-插入联系人成功({})==", JsonKit.toJson(contacts));
            } else {
                log.info("==官网认证-企业认证-插入联系人失败({})==", JsonKit.toJson(contacts));
            }
        }
    }

    /**
     * 更新分销商推广关系数据
     *
     * @param param
     * @param crmContext
     */
    private void handleDistributionRelation(SiteMember param, CrmContext crmContext) {
        handleDistributionRelation(crmContext, SiteMemberUserTypeEnum.COMPANY.getCode(), DistributorCertifiedEnum.AUDIT.getCode(), param.getRealName());
    }

    /**
     * 组装网站会员数据
     *
     * @param crmSiteMember
     * @param siteMember
     * @return
     */
    private CrmSiteMember assembleCrmSiteMember(CrmSiteMember crmSiteMember, SiteMember siteMember) {
        if (!Strings.isNullOrEmpty(siteMember.getAuditFailureMsg())) {
            crmSiteMember.setAuditFailureMsg(checkStringEmpty(siteMember.getAuditFailureMsg()));
        }

        crmSiteMember.setAuditStatus(siteMember.getAuditStatus());

        if (siteMember.getAuditSubmitTime() != null) {
            crmSiteMember.setAuditSubmitTime(siteMember.getAuditSubmitTime());
        }

        if (siteMember.getAuthSubStatus() != null) {
            crmSiteMember.setAuthSubStatus(siteMember.getAuthSubStatus());
        }

        if (siteMember.getAuthSubTime() != null) {
            crmSiteMember.setAuthSubTime(siteMember.getAuthSubTime());
        }

        if (!Strings.isNullOrEmpty(siteMember.getBusinessLicenceNum())) {
            crmSiteMember.setBusinessLicenceNum(siteMember.getBusinessLicenceNum());
        }


        if (!Strings.isNullOrEmpty(siteMember.getBusinessLicenceImgUrl())) {
            crmSiteMember.setBusinessLicenceImgUrl(siteMember.getBusinessLicenceImgUrl());
        }

        if (!Strings.isNullOrEmpty(siteMember.getChannel())) {
            crmSiteMember.setChannel(siteMember.getChannel());
        }

        if (!Strings.isNullOrEmpty(siteMember.getCity())) {
            crmSiteMember.setCity(siteMember.getCity());
        }

        if (!Strings.isNullOrEmpty(siteMember.getContactAddress())) {
            crmSiteMember.setContactAddress(siteMember.getContactAddress());
        }

        if (!Strings.isNullOrEmpty(siteMember.getLinkMan())) {
            crmSiteMember.setContactor(siteMember.getLinkMan());
        }

        if (!Strings.isNullOrEmpty(siteMember.getContactNumber())) {
            crmSiteMember.setContactNumber(siteMember.getContactNumber());
        }

        crmSiteMember.setCreateTime(new Date());

        if (!Strings.isNullOrEmpty(crmSiteMember.getCustId())) {
            crmSiteMember.setCustId(crmSiteMember.getCustId());
        }

        if (!Strings.isNullOrEmpty(siteMember.getCustomUse())) {
            crmSiteMember.setCustomUse(siteMember.getCustomUse());
        }

        if (!Strings.isNullOrEmpty(siteMember.getEmail())) {
            crmSiteMember.setEmail(siteMember.getEmail());
        }


        if (siteMember.getGmtCreate() != null) {
            crmSiteMember.setGmtCreate(siteMember.getGmtCreate());
        }

        if (siteMember.getGmtModified() != null) {
            crmSiteMember.setGmtModified(siteMember.getGmtModified());
        }

        if (!Strings.isNullOrEmpty(siteMember.getIdImgBackUrl())) {
            crmSiteMember.setIdImgBackUrl(siteMember.getIdImgBackUrl());
        }

        if (!Strings.isNullOrEmpty(siteMember.getIdNum())) {
            String idCard = siteMember.getIdNum() != null ? IdCardEncryptUtil.encrypt(siteMember.getIdNum()) : "";
            crmSiteMember.setIdNum(idCard);
        }

        if (!Strings.isNullOrEmpty(siteMember.getIdImgUrl())) {
            crmSiteMember.setIdImgUrl(siteMember.getIdImgUrl());
        }

        if (!Strings.isNullOrEmpty(siteMember.getLoginName())) {
            crmSiteMember.setLoginName(siteMember.getLoginName());
        }

        if (siteMember.getMaxAppNum() != null) {
            crmSiteMember.setMaxAppNum(siteMember.getMaxAppNum());
        }

        if (siteMember.getMaxNtripUserNum() != null) {
            crmSiteMember.setMaxNtripUserNum(siteMember.getMaxNtripUserNum());
        }

        if (!Strings.isNullOrEmpty(siteMember.getMobile())) {
            crmSiteMember.setMobile(siteMember.getMobile());
        }

        if (siteMember.getNtripUserCount() != null) {
            crmSiteMember.setNtripUserCount(siteMember.getNtripUserCount());
        }

        if (!Strings.isNullOrEmpty(siteMember.getNtripUserPrefix())) {
            crmSiteMember.setNtripPrefixOriginal(siteMember.getNtripUserPrefix());
        }

        if (!Strings.isNullOrEmpty(siteMember.getOptions())) {
            crmSiteMember.setOptions(siteMember.getOptions());
        }

        if (siteMember.getOptionsVer() != null) {
            crmSiteMember.setOptionsVer(siteMember.getOptionsVer());
        }

        if (!Strings.isNullOrEmpty(siteMember.getProv())) {
            crmSiteMember.setProv(siteMember.getProv());
        }

        if (!Strings.isNullOrEmpty(siteMember.getRealName())) {
            crmSiteMember.setRealName(siteMember.getRealName());
        }

        if (!Strings.isNullOrEmpty(siteMember.getRegIp())) {
            crmSiteMember.setRegIp(siteMember.getRegIp());
        }

        if (siteMember.getServiceUse() != null) {
            crmSiteMember.setServiceUse(siteMember.getServiceUse());
        }

        if (siteMember.getId() != null) {
            crmSiteMember.setSiteMemberId(siteMember.getId());
        }

        if (siteMember.getSource() != null) {
            crmSiteMember.setSource(siteMember.getSource());
        }

        if (siteMember.getStatus() != null) {
            crmSiteMember.setStatus(siteMember.getStatus() != null ? (int) siteMember.getStatus() : null);
        }

        if (siteMember.getTryStatus() != null) {
            crmSiteMember.setTryStatus(siteMember.getTryStatus());
        }

        crmSiteMember.setUpdateTime(new Date());

        if (!Strings.isNullOrEmpty(siteMember.getUserFlag())) {
            crmSiteMember.setUserFlag(siteMember.getUserFlag());
        }

        if (!Strings.isNullOrEmpty(siteMember.getUserName())) {
            crmSiteMember.setUserName(siteMember.getUserName());
        }

        crmSiteMember.setUserType(siteMember.getUserType());
        // 添加数据来源,1-网站会员；2-分销商认证。参照ThirdCustomerOriginEnum.java
        crmSiteMember.setDataOrigin(siteMember.getDataOrigin());

        // 添加成为分销商时间
        if (Objects.nonNull(siteMember.getDistriAuditSuccessTime())) {
            crmSiteMember.setDistriAuditSuccessTime(siteMember.getDistriAuditSuccessTime());
        }

        // 冗余推广标签
        if (StringUtils.isNotEmpty(siteMember.getPromotionTag())) {
            crmSiteMember.setPromotionTag(siteMember.getPromotionTag());
        }
        return crmSiteMember;
    }
}
