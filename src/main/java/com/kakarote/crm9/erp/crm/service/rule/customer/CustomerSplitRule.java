package com.kakarote.crm9.erp.crm.service.rule.customer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.kakarote.crm9.common.CrmContext;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.crm.common.CrmCustomerChangeLogEnum;
import com.kakarote.crm9.erp.crm.common.CrmDistributorEnum;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum;
import com.kakarote.crm9.erp.crm.common.customer.FromSourceEnum;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.erp.crm.service.*;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 客户拆分规则
 *
 * @Author: haihong.wu
 * @Date: 2020/6/3 4:28 下午
 */
public class CustomerSplitRule {

    private static final CustomerSplitRule ME = new CustomerSplitRule();

    @Inject
    private CrmSiteMemberService crmSiteMemberService;

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmDistributorPromotionRelationService crmDistributorPromotionRelationService;

    @Inject
    private CrmContactsService crmContactsService;

    @Inject
    private CrmCustomerExtService crmCustomerExtService;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private CrmChangeLogService crmChangeLogService;

    private CustomerSplitRule() {
        Aop.inject(this);
    }

    public static CustomerSplitRule me() {
        return ME;
    }

    /**
     * 执行规则
     *
     * @param context
     */
    public void execute(CrmContext context) {
        CrmCustomer crmCustomer = context.getAs(CrmContext.CRM_CUSTOMER);
        CrmSiteMember siteMember = context.getAs(CrmContext.SITE_MEMBER);
        boolean needSplit = false;
        List<CrmSiteMember> crmSiteMembers = crmSiteMemberService.queryByCustomerId(crmCustomer.getCustomerId());
        if (CollectionUtils.isEmpty(crmSiteMembers)) {
            //没有绑定网站会员，无需拆分
            context.put(CrmContext.CRM_CUSTOMER_AFTER_SPLIT, crmCustomer);
            return;
        }
        for (CrmSiteMember crmSiteMember : crmSiteMembers) {
            if (siteMember.getSiteMemberId().equals(crmSiteMember.getSiteMemberId())) {
                continue;
            }
            if (Integer.valueOf(CrmDistributorEnum.IS_DISTRIBUTOR.getTypes()).equals(crmSiteMember.getIsDistributor())) {
                //3、crm客户关联多个网站会员账号时，当其中二个网站会员都变成分销商身份时，将该网站会员账号生产新的crm客户，原客户上的该网站会员账号删除
                needSplit = true;
                break;
            }
            CrmDistributorPromotionRelation relation = crmDistributorPromotionRelationService.findByUserId(crmSiteMember.getSiteMemberId());
            if (Objects.isNull(crmCustomer.getOwnerUserId()) && (Objects.nonNull(relation) || Objects.nonNull(context.get(CrmContext.DISTRIBUTION_RELATION)))) {
                // 1、crm客户关联多个网站会员账号时，如该客户无负责人，当其中一个网站会员变成二级分销商身份时，将该网站会员账号生产新的crm客户，原客户上的该网站会员账号删除
                // 2、crm客户关联多个网站会员账号时，如该客户无负责人，当其中一个网站会员变成分销终端用户身份时，将该网站会员账号生产新的crm客户，原客户上的该网站会员账号删除
                needSplit = true;
                break;
            }
        }
        //拆分
        if (needSplit) {
            //构建新的客户数据
            CrmCustomer crmCustomerAfterSplit = buildNewCustomer(crmCustomer, siteMember);
            Integer ownerUserId = crmCustomer.getOwnerUserId();
            Integer deptId = crmCustomer.getDeptId();
            //复制负责人(构建的时候就已经复制了)
            if (Objects.nonNull(ownerUserId)) {
                CrmCustomerExt oldCustomerExt = crmCustomerExtService.getByCustomerId(crmCustomer.getCustomerId());
                int storageType = (Objects.isNull(oldCustomerExt) || Objects.isNull(oldCustomerExt.getStorageType())) ? CustomerStorageTypeEnum.RELATE_CAP.getCode() : oldCustomerExt.getStorageType();
                CustomerStorageTypeEnum storageTypeEnum = CustomerStorageTypeEnum.getByCode(storageType);

                //保存客户扩展表
                crmCustomerService.saveCrmCustomerExt(crmCustomerAfterSplit.getCustomerId(), storageType, null, null);

                // 记录日志
                crmRecordService.updateRecord(crmCustomer, crmCustomerAfterSplit, CrmEnum.CUSTOMER_DISTRIBUTE_KEY.getTypes(), null);

                //客户负责人变更记录日志
                crmChangeLogService.saveCustomerChangeLog(storageTypeEnum == CustomerStorageTypeEnum.INSPECT_CAP ? CrmCustomerChangeLogEnum.INSPECT_BD.getCode() : CrmCustomerChangeLogEnum.RELATE_BD.getCode(), crmCustomerAfterSplit.getCustomerId(), ownerUserId.longValue(), null, null);
            }
            if (Objects.nonNull(deptId)) {
                /* 放到同一个部门池 */
                CrmOwnerRecord crmOwnerRecord = new CrmOwnerRecord();
                crmOwnerRecord.setTypeId(crmCustomerAfterSplit.getCustomerId().intValue());
                crmOwnerRecord.setType(8);
                crmOwnerRecord.setPreOwnerUserId(crmCustomerAfterSplit.getOwnerUserId());
                crmOwnerRecord.setCreateTime(DateUtil.date());
                crmOwnerRecord.save();

                //记录客户变更日志
                crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.DEPT.getCode(), crmCustomerAfterSplit.getCustomerId(), null, Long.valueOf(deptId), null);
            }
            //将SiteMember绑定到新的CrmCustomer数据上
            siteMember.setCustId(crmCustomerAfterSplit.getCustomerNo());
            siteMember.update();
            //移动联系人
            CrmContacts crmContacts = crmContactsService.getByCustomerIdAndMobile(crmCustomer.getCustomerId(), siteMember.getMobile());
            if (Objects.nonNull(crmContacts)) {
                crmContacts.setCustomerId(crmCustomerAfterSplit.getCustomerId().intValue());
                crmContacts.update();
            }
            //插入拆分日志
            crmRecordService.addActionRecord(null, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmCustomer.getCustomerId().intValue(), String.format("系统 拆分网站会员账号(网站用户ID:%s)", siteMember.getSiteMemberId()));

            context.put(CrmContext.CRM_CUSTOMER_AFTER_SPLIT, crmCustomerAfterSplit);
        } else {
            context.put(CrmContext.CRM_CUSTOMER_AFTER_SPLIT, crmCustomer);
        }
    }

    private CrmCustomer buildNewCustomer(CrmCustomer sourceCustomer, CrmSiteMember siteMember) {
        CrmCustomerExt oldCustomerExt = crmCustomerExtService.getByCustomerId(sourceCustomer.getCustomerId());
        //复制属性
        CrmCustomer crmCustomer = JSONObject.parseObject(JSONObject.toJSONString(sourceCustomer), CrmCustomer.class);
        crmCustomer.remove("customer_id");
        crmCustomer.remove("customer_no");
        crmCustomer.remove("create_time");
        crmCustomer.remove("update_time");
        crmCustomer.remove("followup");
        crmCustomer.setCustomerName(siteMember.getRealName() + "(" + RandomUtil.randomNumbers(4) + ")");
        crmCustomer.setOriginalCustomerName(siteMember.getRealName());
        crmCustomer.setFromSource(FromSourceEnum.FROM_WEBSITE.getCode());
        crmCustomer.setMobile(siteMember.getMobile());
        if (Objects.nonNull(oldCustomerExt)) {
            crmCustomer.setStorageType(oldCustomerExt.getStorageType());
        }
        //插入客户数据
        R addResult = crmCustomerService.addOrUpdate(new JSONObject().fluentPut("entity", crmCustomer), null);
        if (!addResult.isSuccess()) {
            throw new CrmException("==分销商认证失败-插入客户失败,原因:" + addResult.get("msg") + "==");
        }
        crmCustomer.setCustomerId((Long) ((Map) addResult.get("data")).get("customer_id"));
        crmCustomer.setCustomerNo((String) ((Map) addResult.get("data")).get("customer_no"));

        //插入客户插入日志
        crmRecordService.addRecord(crmCustomer.getCustomerId().intValue(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), null);
        return crmCustomer;
    }
}
