package com.kakarote.crm9.erp.crm.service.handler.customer.sync;

import com.jfinal.kit.JsonKit;
import com.kakarote.crm9.common.CrmContext;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.common.CrmCustomerChangeLogEnum;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmDistributorPromotionRelation;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.erp.crm.service.rule.customer.CustomerSplitRule;
import com.kakarote.crm9.integration.common.DistributorSaleAreaEnum;
import com.kakarote.crm9.integration.common.ThirdCustomerOriginEnum;
import com.kakarote.crm9.integration.entity.DistributorAuditDTO;
import com.kakarote.crm9.integration.entity.SiteMember;
import com.kakarote.crm9.utils.common.BeanMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 官网认证
 *
 * @Author: haihong.wu
 * @Date: 2020/6/1 10:02 上午
 */
@Slf4j
public class DistributorAuditHandler extends BaseSyncHandler {

    //开发198 测试195 生产198
    private Integer distributorDeptId = JfinalConfig.crmProp.getInt("distributor.deptid");
    //开发165 测试165 生产92 我不知道这个是啥，老代码这么写的
    private String distributorLever = JfinalConfig.crmProp.get("distributor.lever");
    //开发167 测试168 生产100
    private String distributorPartner = JfinalConfig.crmProp.get("distributor.partner");

    @Override
    public void handle(Object obj) {
        DistributorAuditDTO param;
        if (obj instanceof DistributorAuditDTO) {
            param = (DistributorAuditDTO) obj;
        } else {
            throw new CrmException("未知参数类型:" + obj.getClass().getSimpleName());
        }
        /* 上下文对象 */
        CrmContext context = new CrmContext();
        /* 查询分销商推广关系 */
        queryDistributorRelation(param.getId(), context);
        /* 客户和网站会员缺失逻辑 */
        checkCustomerAndSiteMember(param, context);
        /* 合并拆分逻辑 */
        splitRule(param, context);
        /* 客户逻辑 */
        handleCustomer(param, context);
        /* 网站会员逻辑 */
        handleSiteMember(param, context);
        /*==附加逻辑==*/
        /* 测量测绘特殊逻辑 */
        surveyingAllocate(param, context);
        /* 同步冗余数据 */
        redundancyParentSiteMemberId(param.getId(), context);
    }

    /**
     * 客户和网站会员缺失逻辑
     * 如果缺失就走企业认证逻辑
     *
     * @param param
     * @param context
     */
    private void checkCustomerAndSiteMember(DistributorAuditDTO param, CrmContext context) {
        // 根据分销商id获取crm客户
        CrmCustomer crmCustomerById = crmCustomerService.queryCustomerBySiteMemberId(param.getId());
        log.info("==分销商认证-crmCustomerById: {}", JsonKit.toJson(crmCustomerById));

        if (Objects.isNull(crmCustomerById)) {
            // 根据官网iD获取不到客户数据,走官网认证逻辑
            CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Website_Company_Audit).handle(constructSiteMember(param));

            // 根据分销商id获取crm客户
            crmCustomerById = crmCustomerService.queryCustomerBySiteMemberId(param.getId());
            log.info("==分销商认证-crmCustomer after completion: {}", JsonKit.toJson(crmCustomerById));
            if (Objects.isNull(crmCustomerById)) {
                throw new CrmException("分销商认证-企业数据补全失败-客户数据为空");
            }
        }
        CrmSiteMember siteMember = crmSiteMemberService.getBySiteMemberId(param.getId());
        log.info("==分销商认证-siteMemberById: {}", JsonKit.toJson(siteMember));
        if (Objects.isNull(siteMember)) {
            throw new CrmException("分销商认证-企业数据补全失败-网站会员数据为空");
        }

        context.put(CrmContext.CRM_CUSTOMER, crmCustomerById);
        context.put(CrmContext.SITE_MEMBER, siteMember);
    }

    /**
     * 合并拆分逻辑
     * <p>
     * 1、crm客户关联多个网站会员账号时，如该客户无负责人，当其中一个网站会员变成二级分销商身份时，将该网站会员账号生产新的crm客户，原客户上的该网站会员账号删除
     * <p>
     * 2、crm客户关联多个网站会员账号时，如该客户无负责人，当其中一个网站会员变成分销终端用户身份时，将该网站会员账号生产新的crm客户，原客户上的该网站会员账号删除
     * <p>
     * 3、crm客户关联多个网站会员账号时，当其中二个网站会员都变成分销商身份时，将该网站会员账号生产新的crm客户，原客户上的该网站会员账号删除
     *
     * @param param
     * @param context
     */
    private void splitRule(DistributorAuditDTO param, CrmContext context) {
        CustomerSplitRule.me().execute(context);
        context.put(CrmContext.CRM_CUSTOMER, context.get(CrmContext.CRM_CUSTOMER_AFTER_SPLIT));
    }


    /**
     * 网站会员逻辑
     * 更新分销商相关信息
     *
     * @param param
     * @param context
     */
    private void handleSiteMember(DistributorAuditDTO param, CrmContext context) {
        CrmSiteMember siteMember = context.getAs(CrmContext.SITE_MEMBER);
        CrmDistributorPromotionRelation relation = context.getAs(CrmContext.DISTRIBUTION_RELATION);
        siteMember.setDistributorStatus(param.getDistributorStatus());
        siteMember.setSaleAreaCode(param.getAreaCode());
        siteMember.setLevel(param.getLevel());
        siteMember.setOperateStatus(param.getOperateStatus());
        siteMember.setIsDistributor(param.getIsDistributor());
        siteMember.setDistriAuditSuccessTime(param.getAuditSuccessTime());
        siteMember.setSaleAreaName(StringUtils.isNotEmpty(param.getAreaName())
                ? param.getAreaName()
                : (StringUtils.isNotEmpty(param.getAreaCode()) ? DistributorSaleAreaEnum.getNameByCode(param.getAreaCode()) : "省份未知"));
        if (Objects.nonNull(relation) && StringUtils.isNotEmpty(relation.getPromotionTag())) {
            // 冗余推广关系信息
            siteMember.setPromotionTag(relation.getPromotionTag());
        }
        if (!siteMember.update()) {
            throw new CrmException("分销商认证-官网用户更新失败");
        }
    }

    /**
     * 客户逻辑
     *
     * @param param
     * @param context
     */
    private void handleCustomer(DistributorAuditDTO param, CrmContext context) {
        CrmCustomer crmCustomer = context.getAs(CrmContext.CRM_CUSTOMER);
        crmCustomer.setDistributor(distributorLever);
        crmCustomer.setPartner(distributorPartner);
        if (!crmCustomer.update()) {
            throw new CrmException("分销商认证-客户数据更新失败");
        }
    }

    /**
     * 测量测绘特殊逻辑
     * 3、当客户有负责人时，系统判断客户负责人的部门
     * 2.1、负责人的部门为数字地信事业部（包含下属部门）：客户的归属关系不变
     * 2.2、负责人的部门为非数字地信事业部（包含下属部门）：系统自动将客户转移到数字地信事业部客户池，并且清空负责人
     *
     * @param param
     * @param context
     */
    private void surveyingAllocate(DistributorAuditDTO param, CrmContext context) {
        CrmCustomer crmCustomer = context.getAs(CrmContext.CRM_CUSTOMER);
        Integer ownerDeptId = null;
        if (Objects.nonNull(crmCustomer.getOwnerUserId())) {
            AdminUser adminUser = adminUserService.getAdminUserByUserId(crmCustomer.getOwnerUserId().longValue());
            ownerDeptId = Objects.isNull(adminUser) ? null : adminUser.getDeptId();
        }
        List<Integer> distributorDeptIdList = adminUserService.queryMyDeptAndSubDeptId(distributorDeptId);
        if (!distributorDeptIdList.contains(ownerDeptId)) {
            //负责人的部门为非数字地信事业部（包含下属部门）：系统自动将客户转移到数字地信事业部客户池，并且清空负责人
            crmCustomer.setDeptId(distributorDeptId);
            crmCustomer.setOwnerUserId(null);

            crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.DEPT.getCode(), crmCustomer.getCustomerId(), null, Long.valueOf(distributorDeptId), null);
            if (!crmCustomer.update()) {
                throw new CrmException("分销商认证-转移测量测绘部门池失败");
            }
        }
    }

    /**
     * 构建SiteMember参数
     *
     * @param distributorAuditDTO
     * @return
     */
    private SiteMember constructSiteMember(DistributorAuditDTO distributorAuditDTO) {
        SiteMember siteMember = new SiteMember();
        BeanMapper.copy(distributorAuditDTO, siteMember);

        // 添加客户来源：2-分销商认证
        siteMember.setDataOrigin(ThirdCustomerOriginEnum.DISTRIBUTOR_AUDIT.getCode());
        siteMember.setDistriAuditSuccessTime(distributorAuditDTO.getAuditSuccessTime());
        return siteMember;
    }
}
