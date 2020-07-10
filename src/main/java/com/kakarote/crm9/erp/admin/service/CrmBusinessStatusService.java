package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.date.DateTime;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessStatusDto;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessStatusSalesActivityDto;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessStatusVerificationDto;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessStatus;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessStatusSalesActivity;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessStatusVerification;
import com.kakarote.crm9.erp.admin.entity.base.BaseCrmBusinessStatus;
import com.kakarote.crm9.erp.admin.entity.base.BaseCrmBusinessStatusSalesActivity;
import com.kakarote.crm9.erp.admin.entity.base.BaseCrmBusinessStatusVerification;
import com.kakarote.crm9.erp.crm.common.CrmBusinessStatusOpenEnum;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author liming.guo
 * 商机阶段service
 */
public class CrmBusinessStatusService {

    @Inject
    private CrmBusinessStatusSalesActivityService crmBusinessStatusSalesActivityService;
    @Inject
    private CrmBusinessStatusVerificationService crmBusinessStatusVerificationService;
    @Inject
    private CrmBusinessService crmBusinessService;


    /**
     * 通过商机组ID查询商机阶段列表
     *
     * @param groupId
     * @return
     */
    public R queryBusinessStatusList(Long groupId) {
        if (Objects.isNull(groupId)) {
            return R.error(CrmErrorInfo.PARAMS_NOT_EXSIT);
        }
        List<CrmBusinessStatus> statusList = selectAllByTypeId(groupId);
        if (CollectionUtils.isEmpty(statusList)) {
            return R.ok().put("data", Collections.emptyList());
        }
        List<R> rList = statusList.stream().map(p -> queryBusinessStatusDetail(p.getStatusId())).collect(Collectors.toList());
        List<CrmBusinessStatusDto> statusDtos = rList.stream().filter(R::isSuccess)
                .map(p -> (CrmBusinessStatusDto) p.get("data")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(statusDtos)) {
            return R.ok().put("data", Collections.emptyList());
        }
        statusDtos.sort(Comparator.comparing(CrmBusinessStatusDto::getOrderNum));
        return R.ok().put("data", statusDtos);
    }

    /**
     * 通过商机组ID查询商机阶段列表
     *
     * @param groupId
     * @return
     */
    public List<CrmBusinessStatus> selectAllByTypeId(Long groupId) {
        return CrmBusinessStatus.dao.find(Db.getSql("crm.business.status.selectAllByTypeId"), groupId);
    }


    /**
     * 保存商机阶段
     *
     * @return
     */
    public R addBusinessStatus(CrmBusinessStatusDto crmBusinessStatusDto) {
        if (Objects.isNull(crmBusinessStatusDto.getGroupId())) {
            return R.error("商机组编号不能为空");
        }
        if (Objects.nonNull(crmBusinessStatusDto.getStatusId())) {
            return R.error("商机阶段ID已存在");
        }
        if (StringUtils.isBlank(crmBusinessStatusDto.getStatusName())) {
            return R.error("商机阶段名称不能为空");
        }
        if (Objects.isNull(crmBusinessStatusDto.getRate())) {
            return R.error("可得性设置不能为空");
        }
        try {
            BigDecimal rate = crmBusinessStatusDto.getRate();
            if (rate.compareTo(BigDecimal.ZERO) < 0) {
                return R.error("可得性设置不合法");
            }
        } catch (Exception e) {
            return R.error("可得性设置不合法");
        }
        CrmBusinessStatusOpenEnum statusOpenEnum = CrmBusinessStatusOpenEnum.getByStatus(crmBusinessStatusDto.getOpened());
        if (Objects.isNull(statusOpenEnum)) {
            return R.error("商机阶段启用状态不能为空");
        }
        List<CrmBusinessStatusSalesActivityDto> activityList = crmBusinessStatusDto.getActivityList();
        if (CollectionUtils.isNotEmpty(activityList)) {
            boolean empty = activityList.stream().anyMatch(p -> Objects.isNull(p.getActivityName()));
            if (empty) {
                return R.error("关键销售活动名称不能为空");
            }
        }
        List<CrmBusinessStatusVerificationDto> verificationList = crmBusinessStatusDto.getVerificationList();
        if (CollectionUtils.isNotEmpty(verificationList)) {
            boolean empty = verificationList.stream().anyMatch(p -> Objects.isNull(p.getVerificationName()));
            if (empty) {
                return R.error("可验证结果名称不能为空");
            }
        }
        crmBusinessStatusDto.setCreateUserId(BaseUtil.getUserId());
        return Db.tx(() -> saveBusinessStatus(crmBusinessStatusDto)) ? R.ok() : R.error("商机阶段保存失败，请重试");
    }

    /**
     * 保存商机阶段信息
     *
     * @param crmBusinessStatusDto
     * @return
     */
    private boolean saveBusinessStatus(CrmBusinessStatusDto crmBusinessStatusDto) {
        CrmBusinessStatus crmBusinessStatus = new CrmBusinessStatus();
        crmBusinessStatus.setTypeId(crmBusinessStatusDto.getGroupId().intValue());
        crmBusinessStatus.setName(crmBusinessStatusDto.getStatusName());
        crmBusinessStatus.setRate(crmBusinessStatusDto.getRate().toString());
        int orderNum;
        Record record = Db.findFirst(Db.getSql("crm.business.status.selectMaxOrderNumByTypeId"), crmBusinessStatusDto.getGroupId());
        if (Objects.isNull(record) || Objects.isNull(record.get("order_num"))) {
            orderNum = 1;
        } else {
            orderNum = record.getInt("order_num") + 1;
        }
        crmBusinessStatus.setOrderNum(orderNum);
        crmBusinessStatus.setCreateTime(DateTime.now());
        crmBusinessStatus.setUpdateTime(DateTime.now());
        crmBusinessStatus.setOpened(crmBusinessStatusDto.getOpened());
        crmBusinessStatus.save();
        Long statusId = crmBusinessStatus.getStatusId();
        Long createUserId = crmBusinessStatusDto.getCreateUserId();
        if (Objects.isNull(statusId)) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(crmBusinessStatusDto.getActivityList())) {
            for (CrmBusinessStatusSalesActivityDto activityDto : crmBusinessStatusDto.getActivityList()) {
                CrmBusinessStatusSalesActivity activity = getSaleActivity(statusId, createUserId, activityDto);
                activity.save();
            }
        }
        if (CollectionUtils.isNotEmpty(crmBusinessStatusDto.getVerificationList())) {
            for (CrmBusinessStatusVerificationDto verificationDto : crmBusinessStatusDto.getVerificationList()) {
                CrmBusinessStatusVerification verification = getVerification(statusId, createUserId, verificationDto);
                verification.save();
            }
        }
        return true;
    }

    /**
     * 查询商机阶段详情信息
     *
     * @param statusId
     * @return
     */
    public R queryBusinessStatusDetail(Long statusId) {
        CrmBusinessStatus crmBusinessStatus = CrmBusinessStatus.dao.findById(statusId);
        if (Objects.isNull(crmBusinessStatus)) {
            return R.error("查询商机阶段信息不存在");
        }
        CrmBusinessStatusDto crmBusinessStatusDto = CrmBusinessStatusDto.builder()
                .statusId(crmBusinessStatus.getStatusId())
                .statusName(crmBusinessStatus.getName())
                .groupId(Objects.nonNull(crmBusinessStatus.getTypeId()) ? Long.valueOf(crmBusinessStatus.getTypeId()) : null)
                .rate(StringUtils.isNotEmpty(crmBusinessStatus.getRate()) ? new BigDecimal(crmBusinessStatus.getRate()) : null)
                .opened(crmBusinessStatus.getOpened())
                .orderNum(Objects.nonNull(crmBusinessStatus.getOrderNum()) ? crmBusinessStatus.getOrderNum() : 1)
                .build();
        //查询关键销售活动
        List<CrmBusinessStatusSalesActivity> statusSalesActivities = crmBusinessStatusSalesActivityService.selectAllByStatusId(statusId);
        if (CollectionUtils.isEmpty(statusSalesActivities)) {
            crmBusinessStatusDto.setActivityList(Collections.emptyList());
        } else {
            List<CrmBusinessStatusSalesActivityDto> activityDtos = Lists.newArrayListWithCapacity(statusSalesActivities.size());
            for (CrmBusinessStatusSalesActivity activity : statusSalesActivities) {
                CrmBusinessStatusSalesActivityDto crmBusinessStatusSalesActivityDto = CrmBusinessStatusSalesActivityDto.builder()
                        .activityId(activity.getId())
                        .activityName(activity.getName()).build();
                activityDtos.add(crmBusinessStatusSalesActivityDto);
            }
            crmBusinessStatusDto.setActivityList(activityDtos);
        }
        String activityNames = crmBusinessStatusDto.getActivityList().stream().map(CrmBusinessStatusSalesActivityDto::getActivityName).collect(Collectors.joining("、"));
        crmBusinessStatusDto.setActivityNames(activityNames);
        //查询可验证结果
        List<CrmBusinessStatusVerification> crmBusinessStatusVerifications = crmBusinessStatusVerificationService.selectAllByStatusId(statusId);
        if (CollectionUtils.isEmpty(crmBusinessStatusVerifications)) {
            crmBusinessStatusDto.setVerificationList(Collections.emptyList());
        } else {
            List<CrmBusinessStatusVerificationDto> verificationDtos = Lists.newArrayListWithCapacity(crmBusinessStatusVerifications.size());
            for (CrmBusinessStatusVerification verification : crmBusinessStatusVerifications) {
                CrmBusinessStatusVerificationDto crmBusinessStatusVerificationDto = CrmBusinessStatusVerificationDto.builder()
                        .verificationId(verification.getId())
                        .verificationName(verification.getName()).build();
                verificationDtos.add(crmBusinessStatusVerificationDto);
            }
            crmBusinessStatusDto.setVerificationList(verificationDtos);
        }
        String verificationNames = crmBusinessStatusDto.getVerificationList().stream().map(CrmBusinessStatusVerificationDto::getVerificationName).collect(Collectors.joining("、"));
        crmBusinessStatusDto.setVerificationNames(verificationNames);
        return R.ok().put("data", crmBusinessStatusDto);
    }

    public R updateBusinessStatusInfo(CrmBusinessStatusDto crmBusinessStatusDto) {
        if (Objects.isNull(crmBusinessStatusDto.getStatusId())) {
            return R.error("商机阶段编号不能为空");
        }
        if (StringUtils.isBlank(crmBusinessStatusDto.getStatusName())) {
            return R.error("商机阶段名称不能为空");
        }
        if (Objects.isNull(crmBusinessStatusDto.getRate())) {
            return R.error("可得性设置不能为空");
        }
        CrmBusinessStatusOpenEnum statusOpenEnum = CrmBusinessStatusOpenEnum.getByStatus(crmBusinessStatusDto.getOpened());
        if (Objects.isNull(statusOpenEnum)) {
            return R.error("商机阶段启用状态不能为空");
        }
        List<CrmBusinessStatusSalesActivityDto> activityList = crmBusinessStatusDto.getActivityList();
        if (CollectionUtils.isNotEmpty(activityList)) {
            boolean empty = activityList.stream().anyMatch(p -> Objects.isNull(p.getActivityName()));
            if (empty) {
                return R.error("关键销售活动名称不能为空");
            }
        }
        List<CrmBusinessStatusVerificationDto> verificationList = crmBusinessStatusDto.getVerificationList();
        if (CollectionUtils.isNotEmpty(verificationList)) {
            boolean empty = verificationList.stream().anyMatch(p -> Objects.isNull(p.getVerificationName()));
            if (empty) {
                return R.error("可验证结果名称不能为空");
            }
        }
        crmBusinessStatusDto.setCreateUserId(BaseUtil.getUserId());
        return Db.tx(() -> updateBusinessStatus(crmBusinessStatusDto)) ? R.ok() : R.error("");
    }

    /**
     * 校验商机阶段是否可删除
     *
     * @param statusId
     * @return
     */
    public R checkEnableDelete(Long statusId) {
        CrmBusinessStatus crmBusinessStatus = CrmBusinessStatus.dao.findById(statusId);
        if (Objects.isNull(crmBusinessStatus)) {
            return R.error(CrmErrorInfo.BUSINESS_STATUS_NOT_EXSIT);
        }
        Integer typeId = crmBusinessStatus.getTypeId();
        if (Objects.isNull(typeId)) {
            return R.error(CrmErrorInfo.BUSINESS_GROUP_NOT_EXSIT);
        }
        Integer orderNum = crmBusinessStatus.getOrderNum();
        if (Objects.isNull(orderNum)) {
            orderNum = 1;
        }
        List<Long> statusIds = Lists.newArrayList();
        statusIds.add(statusId);
        List<CrmBusinessStatus> list = selectByTypeIdAndOrderNum(typeId, orderNum);
        if (CollectionUtils.isNotEmpty(list)) {
            statusIds.addAll(list.stream().map(BaseCrmBusinessStatus::getStatusId).collect(Collectors.toList()));
        }
        List<Record> records = crmBusinessService.selectByStatusIds(statusIds);
        if (CollectionUtils.isNotEmpty(records)) {
            return R.error(CrmErrorInfo.BUSINESS_STATUS_DELETE_MESSAGE);
        }
        return R.ok();
    }

    /**
     * 商机阶段删除
     *
     * @param statusId 商机阶段ID
     * @return
     */
    public R deleteBusinessStatus(Long statusId) {
        CrmBusinessStatus crmBusinessStatus = CrmBusinessStatus.dao.findById(statusId);
        if (Objects.isNull(crmBusinessStatus)) {
            return R.error(CrmErrorInfo.BUSINESS_STATUS_NOT_EXSIT);
        }
        R result = checkEnableDelete(statusId);
        if (!result.isSuccess()) {
            return result;
        }
        crmBusinessStatus.setIsDeleted(1);
        crmBusinessStatus.update();
        return R.ok();
    }

    /**
     * 校验商机阶段是否可以封存
     *
     * @param statusId
     * @return
     */
    public R checkEnableClose(Long statusId) {
        /**
         *判断是否可以封存逻辑：
         * 1.首先需要判断：“该阶段是否是某商机的当前阶段”
         * 2.判断“某商机下是否维护过该阶段对应的关键销售活动或可验证结果”
         */
        CrmBusinessStatus crmBusinessStatus = CrmBusinessStatus.dao.findById(statusId);
        if (Objects.isNull(crmBusinessStatus)) {
            return R.error("您好，查询商机阶段不存在");
        }
        //查询当前阶段下的商机
        List<Long> statusList = Lists.newArrayList();
        statusList.add(statusId);
        List<Record> records = crmBusinessService.selectByStatusIds(statusList);
        String message = null;
        if (CollectionUtils.isNotEmpty(records)) {
            message = "该阶段是某些商机的当前阶段，被封存后，这些商机下还会展示。" + "\n" + "您是否确定封存该阶段？";
            return R.ok(message);
        }
        //查询是否有商机绑定关键活动
        List<CrmBusinessStatusSalesActivity> statusSalesActivities = crmBusinessStatusSalesActivityService.selectAllByStatusId(statusId);
        if (CollectionUtils.isNotEmpty(statusSalesActivities)) {
            List<Long> activityIds = statusSalesActivities.stream().map(BaseCrmBusinessStatusSalesActivity::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(activityIds)) {
                List<Record> activityRecords = crmBusinessStatusSalesActivityService.selectActivityRecordsByActivityIds(activityIds);
                if (CollectionUtils.isNotEmpty(activityRecords)) {
                    message = "该阶段在某些商机下维护过关键销售活动或可验证结果，被封存后，这些商机下还会展示。" + "\n" + "您是否确定封存该阶段？";
                    return R.ok(message);
                }
            }
        }
        //查询是否有商机绑定验证结果
        List<CrmBusinessStatusVerification> statusVerifications = crmBusinessStatusVerificationService.selectAllByStatusId(statusId);
        if (CollectionUtils.isNotEmpty(statusVerifications)) {
            List<Long> verificationIds = statusVerifications.stream().map(BaseCrmBusinessStatusVerification::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(verificationIds)) {
                List<Record> verificationRecords = crmBusinessStatusVerificationService.selectVerificationRecordsByVerificationIds(verificationIds);
                if (CollectionUtils.isNotEmpty(verificationRecords)) {
                    message = "该阶段在某些商机下维护过关键销售活动或可验证结果，被封存后，这些商机下还会展示。" + "\n" + "您是否确定封存该阶段？";
                    return R.ok(message);
                }
            }
        }
        return R.ok();
    }

    /**
     * 封存商机阶段
     *
     * @param statusId 商机阶段ID
     * @return
     */
    public R closeBusinessStatus(Long statusId) {
        CrmBusinessStatus crmBusinessStatus = CrmBusinessStatus.dao.findById(statusId);
        if (Objects.isNull(crmBusinessStatus)) {
            return R.error("您好，查询商机阶段不存在");
        }
        if (Objects.nonNull(crmBusinessStatus.getOpened()) && Objects.equals(crmBusinessStatus.getOpened(),
                CrmBusinessStatusOpenEnum.CLOSE.getStatus())) {
            return R.error("您好，商机阶段已经是封存状态，无需重复设置");
        }
        crmBusinessStatus.setOpened(CrmBusinessStatusOpenEnum.CLOSE.getStatus());
        boolean update = crmBusinessStatus.update();
        if (!update) {
            return R.error("您好，商机阶段封存失败，请重试");
        }
        return R.ok();
    }

    /**
     * 解封商机阶段
     *
     * @param statusId 商机阶段ID
     * @return
     */
    public R openBusinessStatus(Long statusId) {
        CrmBusinessStatus crmBusinessStatus = CrmBusinessStatus.dao.findById(statusId);
        if (Objects.isNull(crmBusinessStatus)) {
            return R.error("您好，查询商机阶段不存在");
        }
        if (Objects.nonNull(crmBusinessStatus.getOpened()) && Objects.equals(crmBusinessStatus.getOpened(),
                CrmBusinessStatusOpenEnum.OPEN.getStatus())) {
            return R.error("您好，商机阶段已经是启用状态，无需重复设置");
        }
        crmBusinessStatus.setOpened(CrmBusinessStatusOpenEnum.OPEN.getStatus());
        boolean update = crmBusinessStatus.update();
        if (!update) {
            return R.error("您好，商机阶段启用失败，请重试");
        }
        return R.ok();
    }


    /**
     * 编辑商机阶段
     *
     * @param crmBusinessStatusDto
     * @return
     */
    private boolean updateBusinessStatus(CrmBusinessStatusDto crmBusinessStatusDto) {
        CrmBusinessStatus crmBusinessStatus = CrmBusinessStatus.dao.findById(crmBusinessStatusDto.getStatusId());
        if (Objects.isNull(crmBusinessStatus)) {
            return false;
        }
        Long statusId = crmBusinessStatusDto.getStatusId();
        Long createUserId = crmBusinessStatusDto.getCreateUserId();
        if (StringUtils.isNotBlank(crmBusinessStatusDto.getStatusName())) {
            crmBusinessStatus.setName(crmBusinessStatusDto.getStatusName());
        }
        if (Objects.nonNull(crmBusinessStatusDto.getRate())) {
            crmBusinessStatus.setRate(crmBusinessStatusDto.getRate().toString());
        }
        if (Objects.nonNull(crmBusinessStatusDto.getOpened())) {
            crmBusinessStatus.setOpened(crmBusinessStatusDto.getOpened());
        }
        boolean statusUpdate = crmBusinessStatus.update();
        if (!statusUpdate) {
            return false;
        }
        //查询历史关键销售活动列表
        List<CrmBusinessStatusSalesActivity> oldActivityList = crmBusinessStatusSalesActivityService.selectAllByStatusId(crmBusinessStatusDto.getStatusId());
        Map<Long, CrmBusinessStatusSalesActivity> activityMap = Maps.newHashMapWithExpectedSize(oldActivityList.size());
        for (CrmBusinessStatusSalesActivity activity : oldActivityList) {
            activityMap.put(activity.getId(), activity);
        }
        //查询历史可验证活动列表
        List<CrmBusinessStatusVerification> oldVerificationList = crmBusinessStatusVerificationService.selectAllByStatusId(crmBusinessStatusDto.getStatusId());
        Map<Long, CrmBusinessStatusVerification> verificationMap = Maps.newHashMapWithExpectedSize(oldVerificationList.size());
        for (CrmBusinessStatusVerification verification : oldVerificationList) {
            verificationMap.put(verification.getId(), verification);
        }
        //新修改传递activityId参数集合
        List<Long> newActivityIds = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(crmBusinessStatusDto.getActivityList())) {
            newActivityIds.addAll(crmBusinessStatusDto.getActivityList().stream().filter(p -> Objects.nonNull(p.getActivityId()))
                    .map(CrmBusinessStatusSalesActivityDto::getActivityId).collect(Collectors.toList()));
        }
        //新修改传递verificationId参数集合
        List<Long> newVerificationIds = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(crmBusinessStatusDto.getVerificationList())) {
            newVerificationIds.addAll(crmBusinessStatusDto.getVerificationList().stream().filter(p -> Objects.nonNull(p.getVerificationId()))
                    .map(CrmBusinessStatusVerificationDto::getVerificationId).collect(Collectors.toList()));
        }
        //比对历史的活动和最新的活动编号，判断是否需要删除（逻辑删除）
        oldActivityList.forEach(activity -> {
            if (!newActivityIds.contains(activity.getId())) {
                activity.setIsDeleted(1);
                activity.update();
            }
        });
        //比对历史的可验证结果和最新的可验证结果编号，判断是否需要删除（逻辑删除）
        oldVerificationList.forEach(verification -> {
            if (!newVerificationIds.contains(verification.getId())) {
                verification.setIsDeleted(1);
                verification.update();
            }
        });
        //对比历史活动，不存在则新增，存在则更新
        if (CollectionUtils.isNotEmpty(crmBusinessStatusDto.getActivityList())) {
            crmBusinessStatusDto.getActivityList().forEach(activityDto -> {
                CrmBusinessStatusSalesActivity oldActivity = activityMap.get(activityDto.getActivityId());
                if (Objects.isNull(oldActivity)) {
                    CrmBusinessStatusSalesActivity newActivity = getSaleActivity(statusId, createUserId, activityDto);
                    newActivity.save();
                } else {
                    oldActivity.setName(activityDto.getActivityName());
                    oldActivity.update();
                }
            });
        }
        //对比历史可验证结果，不存在则新增，存在则更新
        if (CollectionUtils.isNotEmpty(crmBusinessStatusDto.getVerificationList())) {
            crmBusinessStatusDto.getVerificationList().forEach(verificationDto -> {
                CrmBusinessStatusVerification oldVerification = verificationMap.get(verificationDto.getVerificationId());
                if (Objects.isNull(oldVerification)) {
                    CrmBusinessStatusVerification newVerification = getVerification(statusId, createUserId, verificationDto);
                    newVerification.save();
                } else {
                    oldVerification.setName(verificationDto.getVerificationName());
                    oldVerification.update();
                }
            });
        }
        oldActivityList.clear();
        oldVerificationList.clear();
        activityMap.clear();
        verificationMap.clear();
        return true;
    }

    /**
     * 获取saleActivity
     *
     * @param statusId
     * @param createUserId
     * @param activityDto
     * @return
     */
    private CrmBusinessStatusSalesActivity getSaleActivity(Long statusId, Long createUserId, CrmBusinessStatusSalesActivityDto activityDto) {
        CrmBusinessStatusSalesActivity activity = new CrmBusinessStatusSalesActivity();
        activity.setName(activityDto.getActivityName());
        activity.setStatusId(statusId);
        activity.setCreateUserId(createUserId);
        activity.setIsDeleted(0);
        activity.setGmtCreate(DateTime.now());
        return activity;
    }

    /**
     * 获取verification
     *
     * @param statusId
     * @param createUserId
     * @param verificationDto
     * @return
     */
    private CrmBusinessStatusVerification getVerification(Long statusId, Long createUserId, CrmBusinessStatusVerificationDto verificationDto) {
        CrmBusinessStatusVerification verification = new CrmBusinessStatusVerification();
        verification.setName(verificationDto.getVerificationName());
        verification.setStatusId(statusId);
        verification.setCreateUserId(createUserId);
        verification.setIsDeleted(0);
        verification.setGmtCreate(DateTime.now());
        return verification;
    }

    /**
     * 获取商机阶段后的所有阶段
     * @param typeId
     * @param orderNum
     * @return
     */
    public List<CrmBusinessStatus> selectByTypeIdAndOrderNum(Integer typeId, Integer orderNum) {
        return CrmBusinessStatus.dao.find(Db.getSql("crm.business.status.selectByTypeIdAndOrderNum"), typeId, orderNum);
    }

    /**
     * 查询商机阶段
     *
     * @param statusId
     * @return
     */
    public CrmBusinessStatus getByStatusId(Long statusId) {
        return CrmBusinessStatus.dao.findById(statusId);
    }

    public String getStatusNameById(Long statusId) {
        if (Objects.isNull(statusId)) {
            return null;
        }
        CrmBusinessStatus businessStatus = getByStatusId(statusId);
        return Objects.nonNull(businessStatus) ? businessStatus.getName() : null;
    }
}
