package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessGroupDetailDto;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessStatusDto;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessGroup;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessStatus;
import com.kakarote.crm9.erp.admin.entity.base.BaseCrmBusinessStatus;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author liming.guo
 * 商机组service
 */
@Slf4j
public class CrmBusinessGroupService {

    @Inject
    private AdminDeptService adminDeptService;
    @Inject
    private AdminUserService adminUserService;
    @Inject
    private CrmBusinessStatusService crmBusinessStatusService;
    @Inject
    private CrmBusinessService crmBusinessService;

    /**
     * 查询商机组列表
     *
     * @return
     */
    public R queryBusinessGroupList() {
        List<CrmBusinessGroup> crmBusinessGroupList = CrmBusinessGroup.dao.find(Db.getSql("crm.businessGroup.selectEffectiveGroups"));
        if (CollectionUtils.isEmpty(crmBusinessGroupList)) {
            return R.ok();
        }
        List<CrmBusinessGroupDetailDto> list = Lists.newArrayListWithCapacity(crmBusinessGroupList.size());
        for (CrmBusinessGroup crmBusinessGroup : crmBusinessGroupList) {
            CrmBusinessGroupDetailDto detailDto = getBusinessGroupDetailDto(crmBusinessGroup);
            list.add(detailDto);
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("list", list);
        return R.ok().put("data", map);
    }

    /**
     * 查询商机组详情
     *
     * @param groupId
     * @return
     */
    public R queryBUsinessGroupDetail(Long groupId) {
        CrmBusinessGroup crmBusinessGroup = CrmBusinessGroup.dao.findById(groupId);
        if (Objects.isNull(crmBusinessGroup)) {
            return R.error(CrmErrorInfo.BUSINESS_GROUP_NOT_EXSIT);
        }
        CrmBusinessGroupDetailDto crmBusinessGroupDetailDto = getBusinessGroupDetailDto(crmBusinessGroup);
        return R.ok().put("data", crmBusinessGroupDetailDto);
    }

    /**
     * 保存商机组
     * @param crmBusinessGroupDetailDto
     * @return
     */
    public R addBusinessGroup(CrmBusinessGroupDetailDto crmBusinessGroupDetailDto) {
        R result = checkDept(crmBusinessGroupDetailDto.getDeptId());
        if (!result.isSuccess()) {
            return result;
        }
        CrmBusinessGroup crmBusinessGroup = new CrmBusinessGroup();
        crmBusinessGroup.setName(crmBusinessGroupDetailDto.getGroupName())
                .setDeptIds(crmBusinessGroupDetailDto.getDeptId())
                .setCreateUserId(BaseUtil.getUserId())
                .setUpdateUserId(BaseUtil.getUserId())
                .setDeptEmailGroup(crmBusinessGroupDetailDto.getGroupEmail())
                .setStatus(1)
                .setIsDeleted(0)
                .setGmtCreate(DateTime.now())
                .setGmtModified(DateTime.now());
        boolean save = crmBusinessGroup.save();
        Long groupId = crmBusinessGroup.getId();
        if (!save) {
            log.error("addBusinessGroup is faild ,crmBusinessGroup:{}", JSONObject.toJSONString(crmBusinessGroup));
            return R.error(CrmErrorInfo.BUSINESS_GROUP_UPDATE_FAILD);
        }
        return R.ok().put("groupId",groupId);
    }


    /**
     * 校验商机组部门是否重复设置
     *
     * @param deptId
     * @return
     */
    public R checkDept(Long deptId) {
        CrmBusinessGroup crmBusinessGroup = CrmBusinessGroup.dao.findFirst(Db.getSql("crm.businessGroup.selectOneByDeptId"), deptId);
        if (Objects.isNull(crmBusinessGroup)) {
            return R.ok();
        }
        Long deptIds = crmBusinessGroup.getDeptIds();
        String message = getMessage(crmBusinessGroup.getName(), deptIds);
        return R.error(message);
    }

    /**
     * 编辑商机组信息
     *
     * @param crmBusinessGroupDetailDto
     * @return
     */
    public R updateBusinessGroup(CrmBusinessGroupDetailDto crmBusinessGroupDetailDto) {
        //判断商机组信息是否存在
        if (Objects.isNull(crmBusinessGroupDetailDto) || Objects.isNull(crmBusinessGroupDetailDto.getGroupId())
                || Objects.isNull(crmBusinessGroupDetailDto.getDeptId())) {
            return R.error(CrmErrorInfo.PARAMS_NOT_EXSIT);
        }
        CrmBusinessGroup oldCrmBusinessGroup = CrmBusinessGroup.dao.findById(crmBusinessGroupDetailDto.getGroupId());
        if (Objects.isNull(oldCrmBusinessGroup)) {
            return R.error(CrmErrorInfo.BUSINESS_GROUP_NOT_EXSIT);
        }
        Long deptId = crmBusinessGroupDetailDto.getDeptId();
        Long groupId = crmBusinessGroupDetailDto.getGroupId();
        CrmBusinessGroup crmBusinessGroup = CrmBusinessGroup.dao.findFirst(Db.getSql("crm.businessGroup.selectOneByDeptId"), deptId);
        //判断部门是否已经设置
        if (Objects.nonNull(crmBusinessGroup) && !Objects.equals(crmBusinessGroup.getId(), groupId)) {
            String message = getMessage(crmBusinessGroup.getName(), deptId);
            return R.error(message);
        }
        oldCrmBusinessGroup.setDeptIds(deptId);
        //保存编辑信息
        if (StringUtils.isNotBlank(crmBusinessGroupDetailDto.getGroupEmail())) {
            oldCrmBusinessGroup.setDeptEmailGroup(crmBusinessGroupDetailDto.getGroupEmail());
        }
        if (StringUtils.isNotBlank(crmBusinessGroupDetailDto.getGroupName())) {
            oldCrmBusinessGroup.setName(crmBusinessGroupDetailDto.getGroupName());
        }
        boolean update = oldCrmBusinessGroup.update();
        if (!update) {
            return R.error(CrmErrorInfo.BUSINESS_GROUP_UPDATE_FAILD);
        }
        return R.ok();
    }

    /**
     * 获取提示信息
     *
     * @param groupName
     * @param deptId
     * @return
     */
    private String getMessage(String groupName, Long deptId) {
        AdminDept adminDept = adminDeptService.getDeptByDeptId(deptId.intValue());
        String deptName = "";
        if (Objects.nonNull(adminDept)) {
            deptName = adminDept.getName();
        }
        return "已针对" + deptName + "设置过商机组" + groupName + "，不允许重复设置。";
    }


    /**
     * 通过CrmBusinessGroup 转化成CrmBusinessGroupDetailDto
     *
     * @param crmBusinessGroup
     * @return
     */
    private CrmBusinessGroupDetailDto getBusinessGroupDetailDto(CrmBusinessGroup crmBusinessGroup) {
        if (Objects.isNull(crmBusinessGroup)) {
            return null;
        }
        CrmBusinessGroupDetailDto detailDto = CrmBusinessGroupDetailDto.builder()
                .groupId(crmBusinessGroup.getId())
                .groupName(crmBusinessGroup.getName())
                .deptId(crmBusinessGroup.getDeptIds())
                .deptName(adminDeptService.getDeptNameTree(crmBusinessGroup.getDeptIds()))
                .groupEmail(crmBusinessGroup.getDeptEmailGroup())
                .createTime(DateUtil.formatDateTime(crmBusinessGroup.getGmtCreate()))
                .updateTime(DateUtil.formatDateTime(crmBusinessGroup.getGmtModified()))
                .build();
        AdminUser createUser = adminUserService.getAdminUserByUserId(crmBusinessGroup.getCreateUserId());
        AdminUser updateUser = adminUserService.getAdminUserByUserId(crmBusinessGroup.getUpdateUserId());
        detailDto.setCreateUserName(Objects.nonNull(createUser) ? createUser.getRealname() : "");
        detailDto.setUpdateUserName(Objects.nonNull(updateUser) ? updateUser.getRealname() : "");
        return detailDto;
    }


    /**
     * 校验商机组下是否有绑定商机
     *
     * @param groupId
     * @return
     */
    public R checkEnableDelete(Long groupId) {
        CrmBusinessGroup oldCrmBusinessGroup = CrmBusinessGroup.dao.findById(groupId);
        if (Objects.isNull(oldCrmBusinessGroup)) {
            return R.error(CrmErrorInfo.BUSINESS_GROUP_NOT_EXSIT);
        }
        List<CrmBusinessStatus> crmBusinessStatusList = crmBusinessStatusService.selectAllByTypeId(groupId);
        if (CollectionUtils.isNotEmpty(crmBusinessStatusList)) {
            List<Long> statusIds = crmBusinessStatusList.stream().map(BaseCrmBusinessStatus::getStatusId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(statusIds)) {
                List<Record> records = crmBusinessService.selectByStatusIds(statusIds);
                if (CollectionUtils.isNotEmpty(records)) {
                    String message = "存在某些商机属于该商机组所对应的部门，一旦删除该商机组则这些商机下的阶段将失效。/n您确定删除此商机组吗？";
                    return R.ok(message);
                }
            }
        }
        return R.ok();
    }

    /**
     * 删除商机组信息
     *
     * @param groupId
     * @return
     */
    public R deleteBusinessGroup(Long groupId) {
        CrmBusinessGroup oldCrmBusinessGroup = CrmBusinessGroup.dao.findById(groupId);
        if (Objects.isNull(oldCrmBusinessGroup)) {
            return R.error(CrmErrorInfo.BUSINESS_GROUP_NOT_EXSIT);
        }
        oldCrmBusinessGroup.setIsDeleted(1);
        boolean update = oldCrmBusinessGroup.update();
        if (!update) {
            return R.error(CrmErrorInfo.BUSINESS_GROUP_UPDATE_FAILD);
        }
        return R.ok();
    }

    /**
     * 复制商机组
     * @param groupId
     * @return
     */
    public R copyBusinessGroup(Long groupId) {
        CrmBusinessGroup oldCrmBusinessGroup = CrmBusinessGroup.dao.findById(groupId);
        if (Objects.isNull(oldCrmBusinessGroup)) {
            return R.error(CrmErrorInfo.BUSINESS_GROUP_NOT_EXSIT);
        }
        return Db.tx(() -> copyBusinessGroup(oldCrmBusinessGroup)) ? R.ok() : R.error("商机组复制失败,请重试");
    }

    /**
     * 复制商机组
     *
     * @param oldCrmBusinessGroup
     * @return
     */
    private boolean copyBusinessGroup(CrmBusinessGroup oldCrmBusinessGroup) {
        Long oldGroupId = oldCrmBusinessGroup.getId();
        CrmBusinessGroup newCrmBusinessGroup = new CrmBusinessGroup();
        newCrmBusinessGroup.setName(oldCrmBusinessGroup.getName() + "（副本）")
                .setCreateUserId(BaseUtil.getUserId())
                .setUpdateUserId(BaseUtil.getUserId())
                .setStatus(1)
                .setIsDeleted(0)
                .setGmtCreate(DateTime.now())
                .setGmtModified(DateTime.now());
        boolean save = newCrmBusinessGroup.save();
        Long groupId = newCrmBusinessGroup.getId();
        if (!save) {
            return false;
        }
        R result = crmBusinessStatusService.queryBusinessStatusList(oldGroupId);
        Object data = result.get("data");
        List<CrmBusinessStatusDto> statusDtos = null;
        if (Objects.nonNull(data) && data instanceof List) {
            statusDtos = (List<CrmBusinessStatusDto>) data;
        }
        if (CollectionUtils.isNotEmpty(statusDtos)) {
            for (CrmBusinessStatusDto statusDto : statusDtos) {
                statusDto.setGroupId(groupId);
                statusDto.setStatusId(null);
                R r = crmBusinessStatusService.addBusinessStatus(statusDto);
                if (!r.isSuccess()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取最近商机组配置的部门
     * @param deptId
     * @return
     */
    public Record getConfiguredDept(Long deptId) {
        if (Objects.isNull(deptId)) {
            return null;
        }
        CrmBusinessGroup businessGroup = CrmBusinessGroup.dao.findFirst(Db.getSql("crm.businessGroup.getConfiguredDept"), deptId);
        if (Objects.nonNull(businessGroup)) {
            AdminDept adminDept = adminDeptService.getDeptByDeptId(businessGroup.getDeptIds().intValue());
            return new Record()
                    .set("typeId", businessGroup.getId())
                    .set("deptId", businessGroup.getDeptIds())
                    .set("bizDeptId", Long.valueOf(adminDeptService.getBusinessDepartmentByDeptIdNew(String.valueOf(businessGroup.getDeptIds()))))
                    .set("deptName", Objects.isNull(adminDept) ? "" : adminDept.getName());
        }
        Long parentDeptId = adminDeptService.getParentDeptId(deptId);
        if (Objects.nonNull(parentDeptId)) {
            return getConfiguredDept(parentDeptId);
        }
        return null;
    }
}
