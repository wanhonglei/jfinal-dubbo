package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.UserExpandInfo;
import com.kakarote.crm9.erp.admin.service.AdminConfigService;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminMenuService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum;
import com.kakarote.crm9.erp.crm.common.customer.DistributorIdentityEnum;
import com.kakarote.crm9.erp.crm.common.customer.FromSourceEnum;
import com.kakarote.crm9.erp.crm.common.customerAscriptionEnum;
import com.kakarote.crm9.erp.crm.common.scene.CrmCustomerSceneEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmCustomerDisposeStatus;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;
import com.kakarote.crm9.erp.crm.service.handler.customer.query.BaseCustomerQueryHandler;
import com.kakarote.crm9.erp.crm.service.handler.customer.query.CustomerQueryHandlers;
import com.kakarote.crm9.erp.crm.vo.CrmCustomerGuideQueryVO;
import com.kakarote.crm9.erp.crm.vo.CustomerQuerySceneVO;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.RecordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/30 5:33 下午
 */
@Slf4j
public class CrmCustomerSceneService {
    /**
     * 推广分销商事业部的部门id，以“,”分割
     */
    private static final String PROMOTION_DISTRIBUTOR_DEPTIDS = JfinalConfig.crmProp.get("customer.distributor.promotion.DeptIds");

    private static final String REALM_PRE = "index_";

    @Inject
    private AdminSceneService adminSceneService;

    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private AdminDataDicService adminDataDicService;

    @Inject
    private AdminMenuService adminMenuService;

    @Inject
    private AdminConfigService adminConfigService;

    /**
     * 根据场景编码进行分发（不支持自定义场景）
     *
     * @param pager
     * @return
     */
    public Page<Record> queryPageWithSceneCode(BasePageRequest<CrmCustomerQueryParamDto> pager) {
        CrmCustomerQueryParamDto param = pager.getData();
        if (Objects.isNull(param)) {
            throw new CrmException("参数不能为空");
        }
        String sceneCode = param.getSceneCode();
        if (Objects.isNull(sceneCode)) {
            throw new CrmException("场景编码不能为空");
        }
        CrmCustomerSceneEnum sceneEnum = CrmCustomerSceneEnum.findByCode(sceneCode);
        if (Objects.isNull(sceneEnum)) {
            //不支持自定义场景
            throw new CrmException("未知场景");
        }
        return queryPage(pager, sceneEnum);
    }

    private Page<Record> queryPage(BasePageRequest<CrmCustomerQueryParamDto> pager, CrmCustomerSceneEnum sceneEnum) {
        BaseCustomerQueryHandler handler = CustomerQueryHandlers.getHandler(sceneEnum);
        if (Objects.isNull(handler)) {
            throw new CrmException("未找到场景Handler");
        }
        return handler.query(pager.getData(), pager.getPage(), pager.getLimit());
    }

    /**
     * 客户引导列表页查询
     */
    public List<Record> queryCustomerGuidePageList(CrmCustomerGuideQueryVO param) {
        List<Record> recordList = Db.find(Db.getSqlPara("crm.customer.queryCustomerGuidePageList",
                Kv.by("customerName", param.getCustomerName())
                        .set("siteMemberId", param.getSiteMemberId())
                        .set("siteMemberName", param.getSiteMemberName())
                        .set("mobile", param.getMobile())));

        recordList.forEach(o -> {
            DistributorIdentityEnum distributorIdentityEnum = DistributorIdentityEnum.getByPromotionTag(o.getStr("distributionIdentity"));
            if (distributorIdentityEnum != null) {
                o.set("distributionIdentity", distributorIdentityEnum.getDesc());
                o.set("distributionIdentityCode", distributorIdentityEnum.getCode());
            } else {
                o.set("distributionIdentityCode", null);
            }
            o.set("canReceive", showDrawButton(o, distributorIdentityEnum));

            if (o.getInt("owner_user_id") != null) {
                //负责人不为空，则部门取对应事业部
                AdminDept dept = adminDeptService.getBusinessDeptByDeptId(Long.valueOf(o.getStr("deptName")));
                if (dept != null) {
                    o.set("deptName", dept.getName());
                }
            }
        });

        return recordList;
    }

    /**
     * 是否显示前往领取按钮
     *
     * @param record
     * @return
     */
    private boolean showDrawButton(Record record, DistributorIdentityEnum distributorIdentityEnum) {
        String condition = record.getStr("customerAscription");

        //网站客户池(保护期外)
        if (customerAscriptionEnum.CUSTOMER_POOL_OUT.getName().equals(condition)) {
            //判断客户分销身份属于 分销伙伴终端用户或二级分销伙伴
            boolean canShow = Objects.equals(distributorIdentityEnum, DistributorIdentityEnum.DISTRIBUTOR_PARTNER_TERMINAL_USER)
                    || Objects.equals(distributorIdentityEnum, DistributorIdentityEnum.LEVEL_TWO_DISTRIBUTOR_PARTNER);
            if (canShow) {
                // 判断登录用户是否是侧脸测绘事业部、在线运营事业部的bd
                int businessDepartmentId;
                UserExpandInfo userExpandInfo = BaseUtil.getUserExpandInfo();
                if (userExpandInfo == null) {
                    businessDepartmentId = Integer.parseInt(adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(BaseUtil.getDeptId())));
                } else {
                    businessDepartmentId = userExpandInfo.getBusinessDepartmentId();
                }

                if (StringUtils.isNotEmpty(PROMOTION_DISTRIBUTOR_DEPTIDS)) {
                    for (String deptId : PROMOTION_DISTRIBUTOR_DEPTIDS.split(",")) {
                        //登录人属于数字地信事业部或在线运营事业部
                        if (Integer.parseInt(deptId) == businessDepartmentId) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        //网站客户池(保护期内) 行业BD
        if (customerAscriptionEnum.CUSTOMER_POOL_IN.getName().equals(condition) || customerAscriptionEnum.INDUSTRY_BD.getName().equals(condition)) {
            return false;
        }

        //部门客户池
        if (customerAscriptionEnum.DEPT_POOL.getName().equals(condition)) {
            List<Long> deptIdList = adminDeptService.queryAllSonDeptIds(record.getLong("dept_id"));

            return deptIdList.contains(Long.valueOf(BaseUtil.getDeptId()));
        }

        //电销客户
        return customerAscriptionEnum.TELEMARKETING_POOL.getName().equals(condition);
    }

    /**
     * 获取客户列表上的筛选条件的下拉项
     * @return
     */
    public Map<String, List<Record>> listAllSelect() {
        Map<String, List<Record>> result = new HashMap<>(9);
        //一次性将tags查出来
        Map<String, Map<String, String>> tagMap = adminDataDicService.queryByTags(
                CrmTagConstant.INDUSTRY,
                CrmTagConstant.CUSTOMER_TYPE,
                CrmTagConstant.CUSTOMER_GRADE
        );
        //客户行业
        result.put("customerIndustry", transMap(tagMap.get(CrmTagConstant.INDUSTRY)));
        //客户类型
        result.put("customerType", transMap(tagMap.get(CrmTagConstant.CUSTOMER_TYPE)));
        //客户等级
        result.put("customerGrade", transMap(tagMap.get(CrmTagConstant.CUSTOMER_GRADE)));
        //最近一次进入网站公海的原因
        List<Record> pubReason = adminDataDicService.queryTagList(CrmTagConstant.PUBLIC_CUSTOMER_REASON);
        result.put("publicPoolReason", pubReason == null ? null : pubReason.stream().map(record -> new Record().set("id", record.getStr("dic_id")).set("name", record.getStr("name"))).collect(Collectors.toList()));
        //客户来源
        result.put("fromSource", transMap(FromSourceEnum.toMap()));
        //分销身份
        result.put("distributorStatus", transMap(DistributorIdentityEnum.toMap()));
        //客户跟进状态
        result.put("disposeStatus", transMap(CrmCustomerDisposeStatus.toMap()));
        //客户库类型
        result.put("storageType", transMap(CustomerStorageTypeEnum.toMap()));
        return result;
    }

    /**
     * map 转换成 list
     *
     * @param data
     * @return
     */
    private List<Record> transMap(Map<String, String> data) {
        List<Record> result = new ArrayList<>();
        if (Objects.nonNull(data)) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                result.add(new Record().set("id", entry.getKey()).set("name", entry.getValue()));
            }
        }
        return result;
    }

    /**
     * 获取上游分销商
     *
     * @param name
     * @return
     */
    public List<Record> fuzzyQuerySubDistributor(String name) {
        return Db.find(Db.getSqlPara("customer.scene.fuzzyQuerySubDistributor", Kv.by("name", name)));
    }

    /**
     * 获取当前登陆人的客户查询场景
     */
    public R queryScene() {
        List<CustomerQuerySceneVO> crmCustomerSceneList = Arrays.stream(CrmCustomerSceneEnum.orderedList()).map(o -> {
            CustomerQuerySceneVO customerQuerySceneVO = new CustomerQuerySceneVO();
            customerQuerySceneVO.setSceneCode(o.getCode());
            customerQuerySceneVO.setSceneName(o.getName());
            return customerQuerySceneVO;
        }).collect(Collectors.toList());

        if (!Objects.requireNonNull(BaseUtil.getUser()).getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
            List<String> realmList = adminMenuService.queryMenuByUserId(BaseUtil.getUserId()).stream().map(o -> o.getStr("realm")).collect(Collectors.toList());
            crmCustomerSceneList = crmCustomerSceneList.stream().filter(o -> realmList.contains(REALM_PRE + o.getSceneCode())).collect(Collectors.toList());
        }

        List<Record> recordList = RecordUtil.toRecordList(crmCustomerSceneList);

        adminSceneService.determineDefaultScene(recordList, record -> record.getStr("sceneName"));

        return R.ok().put("data", recordList);
    }

    /**
     * 根据场景获取客户所属部门
     * @param sceneCode
     * @return
     */
    public Record customerDeptBySceneCode(String sceneCode) {
        CrmCustomerSceneEnum sceneEnum = CrmCustomerSceneEnum.findByCode(sceneCode);
        if (Objects.isNull(sceneEnum)) {
            throw new CrmException("未知场景编码");
        }
        switch (sceneEnum) {
            case MOBILE_SALE_CUSTOMER:
                AdminConfig adminConfig = adminConfigService.queryAdminConfig(CrmConstant.PHONE_SALE_BUSINESS_DEPT_ID);
                if (Objects.nonNull(adminConfig)) {
                    Record root = adminDeptService.getFromBizDeptTree(Long.valueOf(adminConfig.getValue()));
                    if (Objects.nonNull(root)) {
                        root.set("isDefault", Boolean.TRUE);
                    }
                    return root;
                }
                log.error("CrmCustomerSceneService-customerDeptBySceneCode-电销部门id配置丢失");
                return null;
            case DEPT_POOL:
                Integer loginDeptId = BaseUtil.getDeptId();
                //获取配置库容的最近的部门
                Long deptId = adminDeptService.getNearestDeptCapacity(adminDeptService.getDeptMap(), adminDeptService.getDeptCapacityMap(), Long.valueOf(loginDeptId)).getLong("deptId");
                if (Objects.isNull(deptId)) {
                    //空的话就拿事业部
                    String bizDeptId = adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(loginDeptId));
                    deptId = Objects.isNull(bizDeptId) ? null : Long.valueOf(bizDeptId);
                }
                if (Objects.isNull(deptId)) {
                    return null;
                }
                Record root = adminDeptService.getFromBizDeptTree(deptId);
                if (Objects.nonNull(root)) {
                    root.set("isDefault", Boolean.TRUE);
                }
                return root;
            case TAKE_PART_CUSTOMER:
            case MY_SUBORDINATE_CUSTOMER:
            case DISTRIBUTOR_RELATE_POOL:
            case CUSTOMER_ALL:
            case CUSTOMER_CREATED_BY_ME:
                Record root1 = adminDeptService.getFromBizDeptTree(null);
                if (Objects.nonNull(root1)) {
                    root1.set("isDefault", Boolean.FALSE);
                }
                return root1;
            case MINE_CUSTOMER:
            case WEBSITE_POOL:
            default:
                //不可选
                return null;
        }
    }

    /**
     * 没有负责人的情况，清空库容类型和团队成员
     * @param customerIds
     */
    public void clearStorageTypeAndGroupMember(List<Long> customerIds) {
        if (CollectionUtils.isEmpty(customerIds)) {
            return;
        }

        //清空当前客户库容类型
        Db.update(Db.getSqlPara("crm.customerExt.deleteStorageTypeByCustomerIds", Kv.by("customerIds", customerIds)));
        Db.update(Db.getSqlPara("crm.group.member.deleteGroupMemberByObjId", Kv.create().set("objIds", customerIds).set("objType", CrmEnum.CUSTOMER_TYPE_KEY.getTypes())));

    }

}
