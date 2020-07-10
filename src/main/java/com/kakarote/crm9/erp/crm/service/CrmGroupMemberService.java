package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.PowerEnum;
import com.kakarote.crm9.erp.crm.dto.CrmGroupMemberDto;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmGroupMember;
import com.kakarote.crm9.erp.crm.entity.base.BaseCrmCustomer;
import com.kakarote.crm9.erp.crm.vo.CrmGroupMemberVO;
import com.kakarote.crm9.utils.StrUtilExt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/14 2:43 下午
 */
@Slf4j
public class CrmGroupMemberService {

    /**
     * 推广分销商事业部的部门id，以“,”分割
     */
    private static final String PROMOTION_DISTRIBUTOR_DEPT_IDS = JfinalConfig.crmProp.get("customer.distributor.promotion.DeptIds");

    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmBusinessService crmBusinessService;

    /**
     * 获取团队成员
     *
     * @param objId
     * @return
     */
    public List<CrmGroupMemberVO> getMembers(Long objId, Integer objType) {
        List<CrmGroupMemberVO> result = new ArrayList<>();
        //获取Members
        List<CrmGroupMember> groupMembers = listByObjId(objId, objType);
        if (CollectionUtils.isNotEmpty(groupMembers)) {
            for (CrmGroupMember groupMember : groupMembers) {
                result.add(buildMemberVo(groupMember.getUserId(), false, groupMember.getPower()));
            }
        }
        return result;
    }

    /**
     * 查询团队成员
     * @param objId
     * @param userId
     * @param objType
     * @return
     */
    public CrmGroupMember getMember(Long objId, Long userId, Integer objType) {
        return CrmGroupMember.dao.findFirst(Db.getSql("crm.group.member.getMember"), objId, userId, objType);
    }

    public CrmGroupMemberVO buildMemberVo(Long userId, boolean isOwner, Integer power) {
        CrmGroupMemberVO result = new CrmGroupMemberVO();
        AdminUser adminUser = adminUserService.getAdminUserByUserId(userId);
        if (Objects.isNull(adminUser)) {
            throw new CrmException("用户数据不存在");
        }
        AdminDept adminDept = adminDeptService.getDeptNameByDeptId(String.valueOf(adminUser.getDeptId()));
        if (Objects.isNull(adminDept)) {
            throw new CrmException("部门数据不存在");
        }
        result.setId(userId);
        result.setName(adminDept.getName());
        result.setRealname(adminUser.getRealname());
        result.setGroupRole(isOwner ? "负责人" : "普通成员");
        String powerStr;
        if (isOwner) {
            powerStr = "负责人权限";
        } else {
            PowerEnum powerEnum = PowerEnum.getByCode(power);
            if (powerEnum == null) {
                throw new CrmException("未识别权限类型");
            }
            powerStr = powerEnum.getDesc();
        }
        result.setPower(powerStr);
        return result;
    }

    /**
     * 添加团队成员
     *
     * @param objId      对象ID(客户ID或者商机ID)
     * @param memberId   成员UserId
     * @param objType    对象类型 {@link CrmEnum}
     * @param power      权限 {@link PowerEnum}
     * @param changeType Nullable 附加的类型(客户2的时候会同时加到商机的团队)
     */
    public void addMember(Long objId, Long memberId, Integer objType, Integer power, String changeType) {
        addMembers(CrmGroupMemberDto.builder()
                .objIds(Collections.singletonList(objId))
                .memberIds(Collections.singletonList(memberId))
                .objType(objType)
                .power(power)
                .changeTypes(changeType)
                .build());
    }

    /**
     * 新增团队成员s
     *
     * @param crmGroupMemberDto
     */
    public void addMembers(CrmGroupMemberDto crmGroupMemberDto) {
        Integer objType = crmGroupMemberDto.getObjType();
        if (Objects.isNull(objType)) {
            throw new CrmException("请指定对象类型");
        }
        CrmEnum objTypeEnum = CrmEnum.getByType(String.valueOf(objType));
        if (Objects.isNull(objTypeEnum)) {
            throw new CrmException("未知对象类型");
        }
        if (CollectionUtils.isEmpty(crmGroupMemberDto.getObjIds())) {
            throw new CrmException("对象ID不能为空");
        }
        if (CollectionUtils.isEmpty(crmGroupMemberDto.getMemberIds())) {
            throw new CrmException("团队成员不能为空");
        }
        /* 参数校验 */
        addValidate(objTypeEnum, crmGroupMemberDto);
        /* 插入逻辑 */
        doAddMember(objTypeEnum, crmGroupMemberDto);
    }

    /**
     * 删除团队成员
     *
     * @param objId       对象ID(客户ID或者商机ID)
     * @param memberId    成员UserId
     * @param objType     对象类型 {@link CrmEnum}
     * @param changeTypes Nullable 附加的类型(客户2的时候会同时加到商机的团队)
     */
    public void deleteMember(Long objId, Long memberId, Integer objType, String changeTypes) {
        deleteMembers(CrmGroupMemberDto.builder()
                .objIds(Collections.singletonList(objId))
                .memberIds(Collections.singletonList(memberId))
                .objType(objType)
                .changeTypes(changeTypes)
                .build());
    }

    /**
     * 删除团队成员
     *
     * @param crmGroupMemberDto
     */
    public void deleteMembers(CrmGroupMemberDto crmGroupMemberDto) {
        Integer objType = crmGroupMemberDto.getObjType();
        if (Objects.isNull(objType)) {
            throw new CrmException("请指定对象类型");
        }
        CrmEnum objTypeEnum = CrmEnum.getByType(String.valueOf(objType));
        if (Objects.isNull(objTypeEnum)) {
            throw new CrmException("未知对象类型");
        }
        if (CollectionUtils.isEmpty(crmGroupMemberDto.getObjIds())) {
            throw new CrmException("对象ID不能为空");
        }
        if (CollectionUtils.isEmpty(crmGroupMemberDto.getMemberIds())) {
            throw new CrmException("团队成员不能为空");
        }
        /* 参数校验 */
        deleteValidate(objTypeEnum, crmGroupMemberDto);
        /* 插入逻辑 */
        doDeleteMember(objTypeEnum, crmGroupMemberDto);
    }

    public List<CrmGroupMember> listByObjId(Long customerId, Integer objType) {
        return CrmGroupMember.dao.find(Db.getSql("crm.group.member.listByObjId"), customerId, objType);
    }

    /**
     * 判断是否团队成员
     *
     * @param objId   对象ID(客户ID或者商机ID)
     * @param userId  成员UserId
     * @param objType 对象类型 {@link CrmEnum}
     * @return
     */
    public boolean isMember(Long objId, Long userId, Integer objType) {
        return Db.find(Db.getSql("crm.group.member.isMember"), objId, userId, objType).size() > 0;
    }

    /**
     * 校验
     *
     * @param objType
     * @param crmGroupMemberDto
     */
    private void addValidate(CrmEnum objType, CrmGroupMemberDto crmGroupMemberDto) {
        switch (objType) {
            case CUSTOMER_TYPE_KEY:
                addCustomerValidate(crmGroupMemberDto);
                break;
            case BUSINESS_TYPE_KEY:
                addBusinessValidate(crmGroupMemberDto);
                break;
            default:
        }
    }

    /**
     * 插入逻辑
     *
     * @param objType
     * @param crmGroupMemberDto
     */
    private void doAddMember(CrmEnum objType, CrmGroupMemberDto crmGroupMemberDto) {
        switch (objType) {
            case CUSTOMER_TYPE_KEY:
                doAddCustomerMember(crmGroupMemberDto);
                break;
            case BUSINESS_TYPE_KEY:
                doAddBusinessMember(crmGroupMemberDto);
                break;
            default:
        }
    }

    private void deleteValidate(CrmEnum objType, CrmGroupMemberDto crmGroupMemberDto) {
        switch (objType) {
            case CUSTOMER_TYPE_KEY:
                deleteCustomerValidate(crmGroupMemberDto);
                break;
            case BUSINESS_TYPE_KEY:
                deleteBusinessValidate(crmGroupMemberDto);
                break;
            default:
        }
    }

    private void doDeleteMember(CrmEnum objType, CrmGroupMemberDto crmGroupMemberDto) {
        switch (objType) {
            case CUSTOMER_TYPE_KEY:
                doDeleteCustomerMember(crmGroupMemberDto);
                break;
            case BUSINESS_TYPE_KEY:
                doDeleteBusinessMember(crmGroupMemberDto);
                break;
            default:
        }
    }

    /**
     * 客户参数校验
     *
     * @param crmGroupMemberDto
     */
    private void addCustomerValidate(CrmGroupMemberDto crmGroupMemberDto) {
        /* 分销商校验逻辑 有上游分销商的客户不能增加配置部门的BD作为团队成员*/
        validateCustomerSubDistributor(crmGroupMemberDto);
        /* 校验团队成员中是否有Owner */
        validateCustomerOwner(crmGroupMemberDto);
    }

    /**
     * 分销商校验逻辑 有上游分销商的客户不能增加配置部门的BD作为团队成员
     *
     * @param crmGroupMemberDto
     */
    private void validateCustomerSubDistributor(CrmGroupMemberDto crmGroupMemberDto) {
        if (StringUtils.isNoneBlank(PROMOTION_DISTRIBUTOR_DEPT_IDS)) {
            List<String> deptIds = Arrays.asList(PROMOTION_DISTRIBUTOR_DEPT_IDS.split(","));
            //组装部门名称
            Map<Long, String> deptMap = new LinkedHashMap<>(deptIds.size());
            for (String deptId : deptIds) {
                AdminDept deptNameByDeptId = adminDeptService.getDeptNameByDeptId(deptId);
                if (Objects.nonNull(deptNameByDeptId) && StringUtils.isNoneBlank(deptNameByDeptId.getName())) {
                    deptMap.put(Long.valueOf(deptId), deptNameByDeptId.getName());
                }
            }
            String deptNames = StringUtils.join(deptMap.values(), "和");
            //判断成员用户的事业部ID是否在配置的ID中
            List<Long> memberIds = crmGroupMemberDto.getMemberIds().stream().filter(memberId -> deptMap.containsKey(adminUserService.getBusinessDepartmentOfUserById(memberId))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(memberIds)) {
                //判断是否有上游分销商
                List<Record> records = crmCustomerService.checkParentDistributor(crmGroupMemberDto.getObjIds().stream().map(Long::intValue).collect(Collectors.toList()));
                if (CollectionUtils.isNotEmpty(records)) {
                    Record record = records.get(0);
                    throw new CrmException(String.format("客户%s已被分销商%s绑定;\r\n——%s的同学不可作为其团队成员:%s", record.getStr("customerName"), record.getStr("realName"), deptNames,
                            memberIds.stream().map(memberId -> {
                                AdminUser adminUser = adminUserService.getAdminUserByUserId(memberId);
                                if (Objects.nonNull(adminUser)) {
                                    return adminUser.getRealname();
                                } else {
                                    return "";
                                }
                            }).collect(Collectors.joining(",")))
                    );
                }
            }
        }
    }

    /**
     * 校验团队成员中是否有Owner
     *
     * @param crmGroupMemberDto
     */
    private void validateCustomerOwner(CrmGroupMemberDto crmGroupMemberDto) {
        //查询客户的OwnerUserIds
        List<Integer> objOwnerIds = crmCustomerService.queryCustomerListByCustomerIds(crmGroupMemberDto.getObjIds()).stream().map(BaseCrmCustomer::getOwnerUserId).collect(Collectors.toList());
        if (crmGroupMemberDto.getMemberIds().stream()
                //成员中包含ownerUserId
                .anyMatch(memberId -> objOwnerIds.contains(memberId.intValue()))
        ) {
            throw new CrmException("负责人不能重复选为团队成员!");
        }
    }

    /**
     * 客户插入逻辑
     *
     * @param crmGroupMemberDto
     */
    @Before(Tx.class)
    private void doAddCustomerMember(CrmGroupMemberDto crmGroupMemberDto) {
        boolean addBusinessMember = false;
        if (StringUtils.isNoneBlank(crmGroupMemberDto.getChangeTypes())) {
            String[] changeTypeArr = crmGroupMemberDto.getChangeTypes().split(",");
            for (String changeType : changeTypeArr) {
                if ("2".equals(changeType)) {
                    addBusinessMember = true;
                    break;
                }
            }
        }
        CrmGroupMember member = new CrmGroupMember();
        for (Long customerId : crmGroupMemberDto.getObjIds()) {
            for (Long memberId : crmGroupMemberDto.getMemberIds()) {
                AdminUser adminUser = adminUserService.getAdminUserByUserId(memberId);
                if (Objects.isNull(adminUser)) {
                    throw new CrmException(String.format("用户[%s]数据不存在", memberId));
                }
                try {
                    /* 插入客户Member */
                    member.clear()
                            .setObjId(customerId)
                            .setObjType(Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()))
                            .setUserId(memberId)
                            .setUserName(adminUser.getRealname())
                            .setPower(crmGroupMemberDto.getPower())
                            .save();
                    /* 判断插入商机Member */
                    if (addBusinessMember) {
                        List<Long> businessIds = crmCustomerService.getBusinessIdListByCustomerIds(crmGroupMemberDto.getObjIds());
                        if (CollectionUtils.isNotEmpty(businessIds)) {
                            addMembers(CrmGroupMemberDto.builder()
                                    .objIds(businessIds)
                                    .memberIds(crmGroupMemberDto.getMemberIds())
                                    .objType(Integer.valueOf(CrmEnum.BUSINESS_TYPE_KEY.getTypes()))
                                    .power(crmGroupMemberDto.getPower())
                                    .build());
                        }
                    }
                } catch (ActiveRecordException e) {
                    if (e.getMessage().contains(BaseConstant.MYSQL_INTEGRITY_CONSTRAINT_VIOLATION_EXCEPTION_NAME)) {
                        log.warn(String.format("[addMember]-客户[%s]已存在团队成员[%s]", customerId, adminUser.getRealname()), e);
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * 商机参数校验
     *
     * @param crmGroupMemberDto
     */
    private void addBusinessValidate(CrmGroupMemberDto crmGroupMemberDto) {

    }

    /**
     * 商机插入逻辑
     *
     * @param crmGroupMemberDto
     */
    private void doAddBusinessMember(CrmGroupMemberDto crmGroupMemberDto) {
        CrmBusiness crmBusiness = new CrmBusiness();
        crmBusiness.setIds(StrUtilExt.join(",", crmGroupMemberDto.getObjIds()));
        crmBusiness.setMemberIds(StrUtilExt.join(",", crmGroupMemberDto.getMemberIds()));
        crmBusiness.setPower(crmGroupMemberDto.getPower());
        crmBusinessService.addMember(crmBusiness);
    }

    private void deleteCustomerValidate(CrmGroupMemberDto crmGroupMemberDto) {
        if (StringUtils.isNoneBlank(PROMOTION_DISTRIBUTOR_DEPT_IDS)) {
            List<String> deptIds = Arrays.asList(PROMOTION_DISTRIBUTOR_DEPT_IDS.split(","));
            //组装部门名称
            Map<Long, String> deptMap = new LinkedHashMap<>(deptIds.size());
            for (String deptId : deptIds) {
                AdminDept deptNameByDeptId = adminDeptService.getDeptNameByDeptId(deptId);
                if (Objects.nonNull(deptNameByDeptId) && StringUtils.isNoneBlank(deptNameByDeptId.getName())) {
                    deptMap.put(Long.valueOf(deptId), deptNameByDeptId.getName());
                }
            }
            //判断成员用户的事业部ID是否在配置的ID中
            List<Long> memberIds = crmGroupMemberDto.getMemberIds().stream().filter(memberId -> deptMap.containsKey(adminUserService.getBusinessDepartmentOfUserById(memberId))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(memberIds)) {
                //判断是否有上游分销商
                List<Record> records = crmCustomerService.checkParentDistributor(crmGroupMemberDto.getObjIds().stream().map(Long::intValue).collect(Collectors.toList()));
                if (CollectionUtils.isNotEmpty(records)) {
                    Record record = records.get(0);
                    throw new CrmException(String.format("所勾选的客户中，%s已被分销商绑定，不允许分派/添加团队成员/移出团队成员，请重新选择", record.getStr("customerName")));
                }
            }
        }
    }

    @Before(Tx.class)
    private void doDeleteCustomerMember(CrmGroupMemberDto crmGroupMemberDto) {
        boolean deleteBusinessMember = false;
        if (StringUtils.isNoneBlank(crmGroupMemberDto.getChangeTypes())) {
            String[] changeTypeArr = crmGroupMemberDto.getChangeTypes().split(",");
            for (String changeType : changeTypeArr) {
                //别问我为啥，老逻辑就这样
                if ("2".equals(changeType)) {
                    deleteBusinessMember = true;
                    break;
                }
            }
        }
        /* 遍历删除 */
        for (Long objId : crmGroupMemberDto.getObjIds()) {
            for (Long memberId : crmGroupMemberDto.getMemberIds()) {
                CrmGroupMember member = getMember(objId, memberId, crmGroupMemberDto.getObjType());
                if (Objects.nonNull(member)) {
                    member.delete();
                }
            }
        }
        if (deleteBusinessMember) {
            List<Long> businessIds = crmCustomerService.getBusinessIdListByCustomerIds(crmGroupMemberDto.getObjIds());
            if (CollectionUtils.isNotEmpty(businessIds)) {
                deleteMembers(CrmGroupMemberDto.builder()
                        .objIds(businessIds)
                        .memberIds(crmGroupMemberDto.getMemberIds())
                        .objType(Integer.valueOf(CrmEnum.BUSINESS_TYPE_KEY.getTypes()))
                        .build());
            }
        }
    }

    private void deleteBusinessValidate(CrmGroupMemberDto crmGroupMemberDto) {

    }

    private void doDeleteBusinessMember(CrmGroupMemberDto crmGroupMemberDto) {
        CrmBusiness crmBusiness = new CrmBusiness();
        crmBusiness.setIds(StrUtilExt.join(",", crmGroupMemberDto.getObjIds()));
        crmBusiness.setMemberIds(StrUtilExt.join(",", crmGroupMemberDto.getMemberIds()));
        crmBusiness.setPower(crmGroupMemberDto.getPower());
        crmBusinessService.deleteMembers(crmBusiness);
    }

    public List<CrmGroupMember> queryByObjIds(List<Long> objIds,Integer objType) {
        return CrmGroupMember.dao.find(Db.getSqlPara("crm.group.member.queryByObjIds", Kv.by("objIds", objIds).set("objType", objType)));
    }
}
