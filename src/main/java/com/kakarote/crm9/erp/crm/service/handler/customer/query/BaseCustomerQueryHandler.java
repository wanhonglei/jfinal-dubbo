package com.kakarote.crm9.erp.crm.service.handler.customer.query;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Maps;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.base.BaseAdminUser;
import com.kakarote.crm9.erp.admin.service.*;
import com.kakarote.crm9.erp.crm.common.CrmDistributorEnum;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.customer.DistributorIdentityEnum;
import com.kakarote.crm9.erp.crm.common.customer.FromSourceEnum;
import com.kakarote.crm9.erp.crm.constant.CrmCustomerDisposeStatus;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;
import com.kakarote.crm9.erp.crm.entity.CrmGroupMember;
import com.kakarote.crm9.erp.crm.service.CrmGroupMemberService;
import com.kakarote.crm9.erp.crm.service.CrmSiteMemberService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/7 4:48 下午
 */
@Slf4j
public abstract class BaseCustomerQueryHandler {

    @Inject
    protected AdminDataDicService adminDataDicService;
    @Inject
    protected AdminSceneService adminSceneService;
    @Inject
    protected AdminConfigService adminConfigService;
    @Inject
    protected AdminUserService adminUserService;
    @Inject
    protected CrmSiteMemberService siteMemberService;
    @Inject
    protected CrmGroupMemberService crmGroupMemberService;
    @Inject
    protected AdminDeptService adminDeptService;
    //特殊参数，有这个直接返回空数据
    protected static final String RETURN_FLAG = "RETURN_FLAG";

    /**
     * 查询
     *
     * @param paramDto 参数
     * @param page     页码
     * @param limit    页大小
     * @return
     */
    public Page<Record> query(CrmCustomerQueryParamDto paramDto, int page, int limit) {
        //组装SQL
        Kv params = buildParam(paramDto);
        if (params.containsKey(RETURN_FLAG)) {
            //带有这个标识位直接返回，不查库
            return new Page<>(null, page, limit, 0, 0);
        }
        SqlPara sqlPara = Db.getSqlPara("customer.scene.queryPageList", params);
        String orderKey = StringUtils.isBlank(paramDto.getOrderKey()) ? "customer.customer_id" : attachAlias(paramDto.getOrderKey());
        String orderType = StringUtils.isBlank(paramDto.getOrderType()) ? "desc" : paramDto.getOrderType();
        //SELECT SQL
        String fromSql = sqlPara.getSql() + " order by " + orderKey + " " + orderType;
        //QUERY
        Page<Record> result = Db.paginate(page, limit, Db.getSql("customer.scene.queryPageColumns"), fromSql, sqlPara.getPara());
        //附加网站会员数据
        appendSiteMember(result);
        //附加团队成员数据
        appendGroupMember(result);
        //翻译客户类型等
        transData(result);
        return result;
    }

    /**
     * 附加别名
     *
     * @param column
     * @return
     */
    private String attachAlias(String column) {
        return Optional.ofNullable(ALIAS.get(column)).orElse(column);
    }

    /**
     * 别名MAP
     */
    private static final Map<String, String> ALIAS = new HashMap<>(5);

    static {
        ALIAS.put("customer_name", "customer.customer_name");
        ALIAS.put("customerName", "customer.customer_name");
        ALIAS.put("address", "customer.address");
        ALIAS.put("lately_follow_time", "customerExt.lately_follow_time");
        ALIAS.put("disposeTime", "customerExt.lately_follow_time");
        ALIAS.put("create_time", "customer.create_time");
        ALIAS.put("createTime", "customer.create_time");
        ALIAS.put("update_time", "customer.update_time");
        ALIAS.put("updateTime", "customer.update_time");
    }

    /**
     * 组装参数
     *
     * @param paramDto
     * @return
     */
    private Kv buildParam(CrmCustomerQueryParamDto paramDto) {
        Kv params = Kv.create();
        //客户名称
        putIfNotBlank(params, "customerName", paramDto.getCustomerName());
        //客户来源
        putIfNotBlank(params, "fromSource", paramDto.getCustomerSourceId());
        //官网用户ID
        putIfNotBlank(params, "siteMemberIds", split(paramDto.getSiteMemberIds(), "官网用户ID"));

        //客户行业
        putIfNotBlank(params, "industryCode", paramDto.getCustomerIndustryId());
        //客户类型
        putIfNotBlank(params, "customerType", paramDto.getCustomerTypeId());
        //位置信息
        putIfNotBlank(params, "address", joinAddress(paramDto.getProvince(), paramDto.getCity(), paramDto.getArea()));
        //客户等级
        putIfNotBlank(params, "customerGrade", paramDto.getCustomerGradeId());
        //分销身份
        if (StringUtils.isNotBlank(paramDto.getDistributionIdentityId())) {
            DistributorIdentityEnum distributorStatus = DistributorIdentityEnum.getByCode(paramDto.getDistributionIdentityId());
            if (ObjectUtil.isNull(distributorStatus)) {
                throw new CrmException("未知分销商身份");
            }
            if (distributorStatus == DistributorIdentityEnum.DISTRIBUTOR) {
                putIfNotBlank(params, "isDistributor", CrmDistributorEnum.IS_DISTRIBUTOR.getTypes());
            } else {
                putIfNotBlank(params, "promotionTag", distributorStatus.getPromotionTag());
            }
        }
        //是否官网客户
        if (Objects.nonNull(paramDto.getIsSiteMember())) {
            if (YesOrNo.yes(paramDto.getIsSiteMember())) {
                putIfNotBlank(params, "isMember", Boolean.TRUE);
            } else {
                putIfNotBlank(params, "noMember", Boolean.TRUE);
            }
        }
        //是否有联系人
        if (Objects.nonNull(paramDto.getHasContacts())) {
            if (YesOrNo.yes(paramDto.getHasContacts())) {
                putIfNotBlank(params, "hasContact", Boolean.TRUE);
            } else {
                putIfNotBlank(params, "noContact", Boolean.TRUE);
            }
        }
        //是否有小记
        if (Objects.nonNull(paramDto.getHasNotes())) {
            if (YesOrNo.yes(paramDto.getHasNotes())) {
                putIfNotBlank(params, "hasRecord", Boolean.TRUE);
            } else {
                putIfNotBlank(params, "noRecord", Boolean.TRUE);
            }
        }
        //是否有商机
        if (Objects.nonNull(paramDto.getHasBusiness())) {
            if (YesOrNo.yes(paramDto.getHasBusiness())) {
                putIfNotBlank(params, "hasBusiness", Boolean.TRUE);
            } else {
                putIfNotBlank(params, "noBusiness", Boolean.TRUE);
            }
        }
        //最后一次进入公海的原因
        putIfNotBlank(params, "lastPublicReason", paramDto.getPutSeasReasonId());
        //负责人
        putIfNotBlank(params, "ownerUserIds", split(paramDto.getOwnerUserIds(), "负责人ID"));
        //团队成员
        putIfNotBlank(params, "memberIds", split(paramDto.getTeamUserIds(), "团队成员ID"));
        //创建人
        putIfNotBlank(params, "createUserIds", split(paramDto.getCreateUserIds(), "创建人ID"));
        //创建时间
        putIfNotBlank(params, "createStartTime", paramDto.getCreateStartTime());
        putIfNotBlank(params, "createEndTime", paramDto.getCreateEndTime());
        //更新时间
        putIfNotBlank(params, "updateStartTime", paramDto.getUpdateStartTime());
        putIfNotBlank(params, "updateEndTime", paramDto.getUpdateEndTime());
        //客户跟进状态
        if (Objects.nonNull(paramDto.getDisposeStatus())) {
            CrmCustomerDisposeStatus disposeStatus = CrmCustomerDisposeStatus.findByCode(paramDto.getDisposeStatus());
            if (Objects.isNull(disposeStatus)) {
                throw new CrmException("未知跟进状态");
            }
            if (disposeStatus == CrmCustomerDisposeStatus.FOLLOWING) {
                putIfNotBlank(params, "following", Boolean.TRUE);
            } else {
                putIfNotBlank(params, "toBeFollow", Boolean.TRUE);
            }
        }
        //客户库类型
        if (Objects.nonNull(paramDto.getStorageType())) {
            params.set("hasOwnerUserId", Boolean.TRUE);
            params.set("storageType", paramDto.getStorageType());
        }
        //上游分销商
        putIfNotBlank(params, "pCustomerId", paramDto.getPCustomerId());
        //联系人名称
        putIfNotBlank(params, "contactName", paramDto.getContactName());
        //联系人手机号码
        putIfNotBlank(params, "contactMobile", paramDto.getMobile());
        return attachParam(paramDto, params);
    }

    /**
     * 客户所属部门
     * 查询出在【该部门及其下属部门的所有客户】或【负责人属于该部门及其下属部门的所有客户】
     *
     * @param params
     * @param paramDto
     */
    protected void customerOwnDept(Kv params, CrmCustomerQueryParamDto paramDto) {
        if (Objects.isNull(paramDto.getDeptId())) {
            return;
        }
        //子部门树
        Record deptTree = adminDeptService.getFromBizDeptTree(paramDto.getDeptId());
        if (Objects.isNull(deptTree)) {
            return;
        }
        //遍历所有部门ID
        List<Long> deptIds = adminDeptService.loopDeptTreeForDeptIdList(deptTree);
        if (CollectionUtils.isEmpty(deptIds)) {
            return;
        }
        //部门池
        params.set("orDeptIds", deptIds);
        //查询部门下所有用户的id
        List<Long> userIds = adminUserService.queryUserIdsByDeptIdList(deptIds);
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        params.set("orOwnerUserIds", userIds);
    }

    /**
     * 附加场景业务条件
     *
     * @param paramDto
     * @param params
     * @return
     */
    protected abstract Kv attachParam(CrmCustomerQueryParamDto paramDto, Kv params);

    /**
     * 附加网站会员数据
     *
     * @param result
     */
    private void appendSiteMember(Page<Record> result) {
        if (Objects.isNull(result) || CollectionUtils.isEmpty(result.getList())) {
            return;
        }
        //结果集中所有的customerId
        List<Long> customerIds = result.getList().stream().map(record -> record.getLong("customer_id")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(customerIds)) {
            return;
        }
        //一次性查询SiteMember
        List<Record> siteMembers = siteMemberService.queryByCustomerIds(customerIds);
        if (CollectionUtils.isEmpty(siteMembers)) {
            return;
        }
        //组装成customerId-List
        Map<Long, List<Record>> memberMap = siteMembers.stream().reduce(new HashMap<>(1), (result1, record) -> {
            Long customerId = record.getLong("customer_id");
            List<Record> list = result1.get(customerId);
            if (Objects.isNull(list)) {
                list = new ArrayList<>();
                result1.put(customerId, list);
            }
            list.add(record);
            return result1;
        }, (longListHashMap, longListHashMap2) -> null);
        if (memberMap.isEmpty()) {
            return;
        }
        //组装数据
        for (Record record : result.getList()) {
            List<Record> siteMemberList = memberMap.get(record.getLong("customer_id"));
            if (CollectionUtils.isNotEmpty(siteMemberList)) {
                //团队成员ID
                record.set("siteMemberIds", siteMemberList.stream().map(siteMember -> siteMember.getStr("site_member_id")).collect(Collectors.joining(",")));
                //分销身份
                Set<String> distributorIdentity = new HashSet<>(1);
                for (Record siteMember : siteMemberList) {
                    String isDistributor = siteMember.getStr("is_distributor");
                    if (CrmDistributorEnum.IS_DISTRIBUTOR.getTypes().equals(isDistributor)) {
                        distributorIdentity.add(DistributorIdentityEnum.DISTRIBUTOR.getDesc());
                    }
                    String promotionTag = siteMember.getStr("promotion_tag");
                    if (StringUtils.isNotBlank(promotionTag)) {
                        DistributorIdentityEnum promotionTagEnum = DistributorIdentityEnum.getByPromotionTag(promotionTag);
                        if (Objects.isNull(promotionTagEnum)) {
                            log.error("BaseCustomerQueryHandler appendSiteMember error:未知推广标签[{}]", promotionTag);
                            distributorIdentity.add(promotionTag);
                        } else {
                            distributorIdentity.add(promotionTagEnum.getDesc());
                        }
                    }
                }
                record.set("distributionIdentityName", StringUtils.join(distributorIdentity, ","));
            }
        }
    }

    /**
     * 附加团队成员数据
     *
     * @param result
     */
    private void appendGroupMember(Page<Record> result) {
        if (Objects.isNull(result) || CollectionUtils.isEmpty(result.getList())) {
            return;
        }
        //结果集中所有的customerId
        List<Long> customerIds = result.getList().stream().map(record -> record.getLong("customer_id")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(customerIds)) {
            return;
        }
        //一次性查询GroupMember
        List<CrmGroupMember> memberList = crmGroupMemberService.queryByObjIds(customerIds, Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()));
        //组装数据
        Map<Long, List<CrmGroupMember>> memberMap = memberList.stream().reduce(new HashMap<>(1), (result1, crmGroupMember) -> {
            Long customerId = crmGroupMember.getObjId();
            List<CrmGroupMember> list = result1.get(customerId);
            if (ObjectUtil.isNull(list)) {
                list = new ArrayList<>();
                result1.put(customerId, list);
            }
            list.add(crmGroupMember);
            return result1;
        }, (longListHashMap, longListHashMap2) -> null);
        //一次性查询所有owner
        Map<Long, AdminUser> ownerUserMap = adminUserService.getUserListByUserIds(
                //先汇总成ownerUserId list
                result.getList().stream().map(record -> record.getLong("owner_user_id")).collect(Collectors.toList()))
                //将结果集reduce成userId-AdminUser的map
                .stream().collect(Collectors.toMap(BaseAdminUser::getUserId, Function.identity()));
        //转换成customerId-owner的AdminUser
        Map<Long, AdminUser> customerOwnerMap = Maps.newHashMapWithExpectedSize(ownerUserMap.size());
        result.getList().forEach(record -> customerOwnerMap.put(record.getLong("customer_id"), ownerUserMap.get(record.getLong("owner_user_id"))));
        //组装结果集
        for (Record record : result.getList()) {
            Long customerId = record.getLong("customer_id");
            List<Record> teamMember = new ArrayList<>();
            //Owner
            AdminUser ownerUser = customerOwnerMap.get(customerId);
            if (Objects.nonNull(ownerUser)) {
                teamMember.add(new Record().set("userId", ownerUser.getUserId()).set("userName", ownerUser.getRealname()).set("isOwner", Boolean.TRUE));
            }
            //团队成员
            List<CrmGroupMember> members = memberMap.get(customerId);
            if (CollectionUtils.isNotEmpty(members)) {
                teamMember.addAll(members.stream().map(crmGroupMember -> new Record().set("userId", crmGroupMember.getUserId()).set("userName", crmGroupMember.getUserName()).set("isOwner", Boolean.FALSE)).collect(Collectors.toList()));
            }
            record.set("teamMember", teamMember);
        }
    }

    /**
     * 翻译字段
     *
     * @param recordPage
     */
    protected void transData(Page<Record> recordPage) {
        if (CollectionUtils.isNotEmpty(recordPage.getList())) {
            List<Record> customerGradeList = adminDataDicService.queryTagList(CrmTagConstant.CUSTOMER_GRADE);
            List<Record> customerTypeList = adminDataDicService.queryTagList(CrmTagConstant.CUSTOMER_TYPE);
            List<Record> industryList = adminDataDicService.queryTagList(CrmTagConstant.INDUSTRY);
            for (Record record : recordPage.getList()) {
                if (Objects.nonNull(record.getLong("create_user_id"))) {
                    AdminUser createUser = adminUserService.getAdminUserByUserId(record.getLong("create_user_id"));
                    if (Objects.nonNull(createUser)) {
                        record.set("create_user_name", createUser.getRealname());
                    }
                }
                record.set("customer_grade", adminSceneService.formatCustomerGradeTagInfo(record.getStr("customer_grade"), customerGradeList));
                record.set("customer_type", adminSceneService.formatCustomerGradeTagInfo(record.getStr("customer_type"), customerTypeList));
                record.set("industry_name", adminSceneService.formatCustomerGradeTagInfo(record.getStr("industry_code"), industryList));
                record.set("fromSourceName", FromSourceEnum.getDescByCode(record.getInt("from_source")));
                AdminUser disposeUser = adminUserService.getAdminUserByUserId(record.getLong("disposeUserId"));
                if (Objects.nonNull(disposeUser)) {
                    record.set("disposeUserName", disposeUser.getRealname());
                }
            }
        }
    }

    /**
     * 将String根据,分开
     *
     * @param data
     * @return
     */
    protected List<Long> split(String data, String itemName) {
        if (StringUtils.isBlank(data)) {
            return null;
        }
        return Arrays.stream(data
                .replaceAll("，", ",")
                .replaceAll(" ", "")
                .split(","))
                .filter(StringUtils::isNotBlank)
                .map(item -> {
                    if (!NumberUtil.isLong(item)) {
                        throw new CrmException(String.format("%s参数格式错误", itemName));
                    }
                    return Long.valueOf(item);
                })
                .collect(Collectors.toList());
    }

    /**
     * 地址拼接
     *
     * @param province 省
     * @param city     市
     * @param area     区
     * @return
     */
    protected String joinAddress(String province, String city, String area) {
        StringBuilder addressBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(province)) {
            addressBuilder.append(province).append(',');
        }
        if (StringUtils.isNotBlank(city)) {
            addressBuilder.append(city).append(',');
        }
        if (StringUtils.isNotBlank(area)) {
            addressBuilder.append(area).append(',');
        }
        int indexOf = addressBuilder.lastIndexOf(",");
        if (indexOf != -1 && indexOf == addressBuilder.length() - 1) {
            addressBuilder.deleteCharAt(addressBuilder.length() - 1);
        }
        return addressBuilder.toString();
    }

    protected void putIfNotBlank(Kv param, String key, Object value) {
        if (Objects.nonNull(value)) {
            if (value instanceof String && StringUtils.isBlank((CharSequence) value)) {
                return;
            }
            param.set(key, value);
        }
    }

    protected enum YesOrNo {
        YES(1),
        NO(0),
        ;

        private Integer code;

        YesOrNo(Integer code) {
            this.code = code;
        }

        private static YesOrNo findByCode(Integer code) {
            for (YesOrNo value : values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }

        public static boolean yes(Integer code) {
            return YES == findByCode(code);
        }

        public Integer getCode() {
            return code;
        }
    }
}
