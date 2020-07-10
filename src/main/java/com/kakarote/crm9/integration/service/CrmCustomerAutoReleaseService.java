package com.kakarote.crm9.integration.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminDeptCapacity;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.base.BaseAdminDeptCapacity;
import com.kakarote.crm9.erp.admin.service.AdminDeptCapacityService;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmCustomerChangeLogEnum;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerReleaseDto;
import com.kakarote.crm9.erp.crm.dto.EffectiveCustomerDto;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.base.BaseCrmBusiness;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.erp.crm.service.CrmChangeLogService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmNotesService;
import com.kakarote.crm9.erp.crm.service.CrmRecordService;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 客户自动释放
 * @author liming.guo
 */
@Slf4j
public class CrmCustomerAutoReleaseService {

   @Inject
   private CrmCustomerService crmCustomerService;
   @Inject
   private AdminUserService adminUserService;
   @Inject
   private AdminDeptService adminDeptService;
   @Inject
   private AdminDeptCapacityService adminDeptCapacityService;
   @Inject
   private CrmNotesService crmNotesService;
   @Inject
   private CrmRecordService crmRecordService;
   @Inject
   private CrmChangeLogService crmChangeLogService;
   @Inject
   private CrmBusinessService crmBusinessService;


   public R customerAutoRelease() {
      log.info("customerAutoRelease is start ,startTime:{}", DateTime.now().toMsStr());
      /**
       * 自动释放逻辑：1 查询配置了出库规则的部门信息 2.根据部门信息查询所有子部门，包含当前部门 3.根据查询的所有部门，查询所有部门下的bd
       * 4.通过bd信息，查询bd负责的，且有配置库存的客户信息,根据配置的出库规则，获取出库限制天数limitDays
       * 5.获取客户的负责时间owner_time，判断owner_time+limitDays>当前时间，说明领取时间在限制时间内，不释放
       * 6.如果owner_time+limitDays<当前时间,再查询客户小记,联系人小记，商机小记，获取领取后的最近一条小记信息，如果大于限制天数，需要释放
       * 7.释放：1.将客户释放回公海，客户库存类型设置成null,记录变更日志 2.如果客户有商机，将商机释放回公海，记录变更日志
       */
      //查询配置了出库规则的所有部门库存规则信息
      List<AdminDeptCapacity> adminDeptCapacities = adminDeptCapacityService.getEnableReleaseAdminDeptCapacityList();
      Map<Long, AdminDeptCapacity> adminDeptCapacityMap = Maps.newHashMap();
      if (CollectionUtils.isNotEmpty(adminDeptCapacities)) {
         for (AdminDeptCapacity adminDeptCapacity : adminDeptCapacities) {
            adminDeptCapacityMap.put(adminDeptCapacity.getDeptId(), adminDeptCapacity);
         }
      }
      if (CollectionUtils.isEmpty(adminDeptCapacities)) {
         log.info("customerAutoRelease is end ,endTme:{},success:true,releaseCount:0,", DateTime.now().toMsStr());
         return R.ok();
      }
      List<Long> adminDeptIds = adminDeptCapacities.stream().map(BaseAdminDeptCapacity::getDeptId).collect(Collectors.toList());
      //通过出库规则的部门查询当前部门和子部门
      List<Long> deptIds = Lists.newArrayList();
      for (Long deptId : adminDeptIds) {
         deptIds.addAll(adminDeptService.queryAllSonDeptIds(deptId));
      }
      //通过部门信息查询部门下的所有bd
      List<AdminUser> adminUsers = adminUserService.getUserListByDeptIds(deptIds);
      Map<Long, AdminUser> adminUserMap = Maps.newHashMap();
      if (CollectionUtils.isNotEmpty(adminUsers)) {
         for (AdminUser adminUser : adminUsers) {
            adminUserMap.put(adminUser.getUserId(), adminUser);
         }
      }

      List<Long> userIds = adminUsers.stream().map(AdminUser::getUserId).collect(Collectors.toList());
      //通过bd信息，查询bd负责的，且有配置库存的客户
      List<EffectiveCustomerDto> effectiveCustomerDtos = crmCustomerService.getEffectiveCustomerDtoListByUserIds(userIds);
      if (CollectionUtils.isEmpty(effectiveCustomerDtos)) {
         log.info("customerAutoRelease is end ,endTme:{},success:true,releaseCount:0,", DateTime.now().toMsStr());
         return R.ok();
      }

      //获取所有部门信息
      List<AdminDept> adminDepts = adminDeptService.getAllAdminDeptList();
      Map<Long, AdminDept> adminDeptMap = Maps.newHashMap();
      if (CollectionUtils.isNotEmpty(adminDepts)) {
         for (AdminDept adminDept : adminDepts) {
            adminDeptMap.put(adminDept.getDeptId(), adminDept);
         }
      }

      List<CrmCustomerReleaseDto> crmCustomerReleaseDtos = Lists.newArrayList();

      for (EffectiveCustomerDto effectiveCustomerDto : effectiveCustomerDtos) {
         Long ownerUserId = effectiveCustomerDto.getOwnerUserId();
         AdminUser user = adminUserMap.get(ownerUserId);
         Long deptId = Long.valueOf(user.getDeptId());
         CustomerStorageTypeEnum storageTypeEnum = CustomerStorageTypeEnum.getByCode(effectiveCustomerDto.getStorageType());
         if (storageTypeEnum == null) {
            log.info("customerAutoRelease storageTypeEnum is null,effectiveCustomerDto:{}", JSONObject.toJSONString(effectiveCustomerDto));
            continue;
         }
         Date ownerTime = effectiveCustomerDto.getOwnerTime();
         //客户领取时间为空，不释放
         if (Objects.isNull(ownerTime)) {
            log.info("customerAutoRelease ownerTime is null,effectiveCustomerDto:{}", JSONObject.toJSONString(effectiveCustomerDto));
            continue;
         }

         //部门一级一级往上匹配，直到找到最近一级配置了出库限制时间的部门
         //如果没有配置出库规则，则不释放客户
         Integer releaseLimitDays = getReleaseDaysByDeptId(adminDeptCapacityMap, adminDeptMap, deptId, storageTypeEnum);
         if (Objects.isNull(releaseLimitDays) || releaseLimitDays <= 0) {
            continue;
         }
         //如果客户领取时间和当前时间间隔小于出库规则限制的天数，不释放
         boolean enableRelease = checkEnableRelease(new Date(), ownerTime, releaseLimitDays);
         if (!enableRelease) {
            continue;
         }
         Long customerId = effectiveCustomerDto.getCustomerId();
         //通过客户,客户下的商机，客户下的联系人查询距离客户领取时间最近的一条联系小记记录,获取联系小记的创建时间
         List<CrmBusiness> crmBusinessList = queryBusinessListByCusotmerId(customerId);
         List<Long> contactIds = queryContactIdsByCustomerId(customerId);
         List<Long> businessIds = crmBusinessList.stream().map(BaseCrmBusiness::getBusinessId).collect(Collectors.toList());
         Date recordCreateTime = queryNearestRecordCreateTimeByCustomerId(customerId, businessIds, contactIds, ownerUserId, ownerTime);
         //如果时间不存在，说明没有记录联系小记，需要释放
         if (Objects.isNull(recordCreateTime)) {
            CrmCustomerReleaseDto crmCustomerReleaseDto = getCrmCustomerReleaseDto(effectiveCustomerDto.getCustomerId(),
                    effectiveCustomerDto.getOwnerUserId(), storageTypeEnum);
            crmCustomerReleaseDto.setCrmBusinessList(crmBusinessList);
            crmCustomerReleaseDtos.add(crmCustomerReleaseDto);
            continue;
         }
         //判断联系小记时间是否需要释放
         // 客户是3月1号领取的，出库规则限制7天 1+(7+1)=9 9号之后（包括9号）记录的联系小记就要释放客户
         enableRelease = checkEnableRelease(recordCreateTime, ownerTime, releaseLimitDays);
         if (enableRelease) {
            CrmCustomerReleaseDto crmCustomerReleaseDto = getCrmCustomerReleaseDto(effectiveCustomerDto.getCustomerId(),
                    effectiveCustomerDto.getOwnerUserId(), storageTypeEnum);
            crmCustomerReleaseDto.setCrmBusinessList(crmBusinessList);
            crmCustomerReleaseDtos.add(crmCustomerReleaseDto);
         }
      }
      if (CollectionUtils.isEmpty(crmCustomerReleaseDtos)) {
         log.info("customerAutoRelease is end ,endTme:{},success:true,releaseCount:0,", DateTime.now().toMsStr());
         return R.ok();
      }
      boolean success;
      int releaseCount;
      try {
         success = autoRelease(crmCustomerReleaseDtos);
         releaseCount = crmCustomerReleaseDtos.size();
      } catch (Exception e) {
         log.error("customerAutoRelease autoRelease is error,message:{}", e.getMessage());
         return R.error();
      }
      log.info("customerAutoRelease is end ,endTme:{},success:{},releaseCount:{},", DateTime.now().toMsStr(),
              success, releaseCount);

      Map<String, Object> map = Maps.newHashMap();
      map.put("success", success);
      map.put("releaseCount", releaseCount);
      return R.ok(map);
   }


   /**
    * 获取设置释放规则设置的限制天数
    */
   private Integer getReleaseDaysByDeptId(Map<Long, AdminDeptCapacity> adminDeptCapacityMap, Map<Long, AdminDept> adminDeptMap,
                                          Long deptId, CustomerStorageTypeEnum storageTypeEnum) {
      Integer releaseDays = null;
      Long queryDeptId = deptId;
      while (true) {
         AdminDept adminDept = adminDeptMap.get(queryDeptId);
         if (Objects.isNull(adminDept)) {
            return releaseDays;
         }
         AdminDeptCapacity adminDeptCapacity = adminDeptCapacityMap.get(queryDeptId);
         Integer days = getReleaseDayByDeptIdAndStorageType(adminDeptCapacity, storageTypeEnum);
         if (Objects.nonNull(days)) {
            return days;
         }
         queryDeptId = Long.valueOf(adminDept.getPid());
      }
   }

   /**
    * 查询出库规则设置的出库限制天数
    */
   private Integer getReleaseDayByDeptIdAndStorageType(AdminDeptCapacity adminDeptCapacity, CustomerStorageTypeEnum storageTypeEnum) {
      if (Objects.isNull(adminDeptCapacity)) {
         return null;
      }
      if (Objects.equals(CustomerStorageTypeEnum.INSPECT_CAP, storageTypeEnum)) {
         if (Objects.equals(adminDeptCapacity.getInspectFlag(), 1) && Objects.nonNull(adminDeptCapacity.getInspectDays())
                 && adminDeptCapacity.getInspectDays() > 0) {
            return adminDeptCapacity.getInspectDays();
         }
      }
      if (Objects.equals(CustomerStorageTypeEnum.RELATE_CAP, storageTypeEnum)) {
         if (Objects.equals(adminDeptCapacity.getRelateOutFlag(), 1) && Objects.nonNull(adminDeptCapacity.getRelateOutDays())
                 && adminDeptCapacity.getRelateOutDays() > 0) {
            return adminDeptCapacity.getRelateOutDays();
         }
      }
      return null;
   }

   /**
    * 判断是否要释放
    * 客户是3月1号领取的，出库规则限制7天 1+(7+1)=9 9号之后（包括9号）记录的联系小记就要释放客户
    *
    * @param createTime
    * @param ownerTime
    * @param days
    * @return
    */
   private boolean checkEnableRelease(Date createTime, Date ownerTime, Integer days) {
      if (Objects.isNull(createTime) || Objects.isNull(ownerTime) || Objects.isNull(days)) {
         return false;
      }
      Date recordCreateTime = DateUtil.beginOfDay(ownerTime);
      Date endTime = DateUtils.addDays(recordCreateTime, (days + 1));
      return createTime.getTime() >= endTime.getTime();
   }

   private CrmCustomerReleaseDto getCrmCustomerReleaseDto(Long customerId, Long ownerUserId, CustomerStorageTypeEnum storageTypeEnum) {
      return CrmCustomerReleaseDto.builder()
              .customerId(customerId)
              .ownerUserId(ownerUserId)
              .storageTypeEnum(storageTypeEnum).build();
   }

   /**
    * 客户自动释放
    *
    * @param crmCustomerReleaseDtos
    */
   @Before(Tx.class)
   private boolean autoRelease(List<CrmCustomerReleaseDto> crmCustomerReleaseDtos) {
      for (CrmCustomerReleaseDto releaseDto : crmCustomerReleaseDtos) {
         //把客户放入公海，客户库存设置成空
         crmCustomerService.pullCustomerPublicPool(releaseDto.getCustomerId());
         crmCustomerService.deleteStorageTypeByCustomerIds(releaseDto.getCustomerId());
         crmRecordService.addPutIntoTheOpenSeaRecord(Collections.singletonList(releaseDto.getCustomerId()),
                 CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), CrmConstant.PUBLIC_POOL + "，原因：客户自动释放");
         Long oldOwnerUserId = releaseDto.getOwnerUserId();
         //记录客户负责人变更记录日志
         crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.OPEN_SEA.getCode(), releaseDto.getCustomerId(), null, null, null);


         //如果客户有绑定商机，将商机释放回公海，记录日志
         if (CollectionUtils.isNotEmpty(releaseDto.getCrmBusinessList())) {
            for (CrmBusiness business : releaseDto.getCrmBusinessList()) {
               if (Objects.nonNull(business.getBusinessId())) {
                  Long businessId = business.getBusinessId();
                  Long ownerUserId = Objects.nonNull(business.getOwnerUserId()) ? Long.valueOf(business.getOwnerUserId()) : null;
                  crmBusinessService.pullBusinessToPublicPool(business.getBusinessId());
                  crmRecordService.addPutIntoTheOpenSeaRecord(Collections.singletonList(business.getBusinessId()),
                          CrmEnum.BUSINESS_TYPE_KEY.getTypes(), CrmConstant.BUSINESS_PUBLIC_POOL + "，原因：客户自动释放");

                  //记录商机负责人变更记录日志
                  crmChangeLogService.saveBusinessChangeLog(CrmCustomerChangeLogEnum.OPEN_SEA.getCode(), businessId, null, null, null);

               }
            }
         }
      }
      TraceIdUtil.remove();
      return true;
   }


   /**
    * 通过客户编号，创建人，客户领取时间查询距离领取时间最近的一条联系小记，获取创建时间
    *
    * @param customerId   客户ID
    * @param createUserId 创建人
    * @param ownerTime    客户领取时间
    * @return
    */
   private Date queryNearestRecordCreateTimeByCustomerId(Long customerId,List<Long> businessIds,List<Long> contactIds,
                                                         Long createUserId, Date ownerTime) {
      if (Objects.isNull(customerId) || Objects.isNull(createUserId) || Objects.isNull(ownerTime)) {
         return null;
      }
      if (CollectionUtils.isEmpty(businessIds)) {
         businessIds = new ArrayList<>();
      }
      if (CollectionUtils.isEmpty(contactIds)) {
         contactIds = new ArrayList<>();
      }
      Kv kv = Kv.by("customerId", customerId).set("createUserId", createUserId).set("ownerTime", ownerTime)
              .set("businessIds", businessIds).set("contactIds", contactIds);
      SqlPara sqlPara = Db.getSqlPara("crm.record.selectNearestRecordByCustomerInfoAndUserId", kv);
      List<Record> recordList = Db.find(sqlPara);
      if (CollectionUtils.isEmpty(recordList)) {
         return null;
      }
      return recordList.get(0).getDate("create_time");
   }


   /**
    * 通过客户ID查询客户下的商机集合
    *
    * @param customerId 客户ID
    * @return
    */
   private List<CrmBusiness> queryBusinessListByCusotmerId(Long customerId) {
      List<CrmBusiness> crmBusinessList = CrmBusiness.dao.find(Db.getSql("crm.business.selectBusinessByCustomerId"), customerId);
      if (CollectionUtils.isEmpty(crmBusinessList)) {
         return Collections.emptyList();
      }
      return crmBusinessList;
   }

   /**
    * 通过客户ID查询联系人ID集合
    *
    * @param customerId 客户ID
    * @return
    */
   private List<Long> queryContactIdsByCustomerId(Long customerId) {
      List<Record> contactsList = Db.find(Db.getSql("crm.contact.selectByCustomerId"), customerId);
      if (CollectionUtils.isEmpty(contactsList)) {
         return Collections.emptyList();
      }
      return contactsList.stream().map(p -> p.getLong("contacts_id")).collect(Collectors.toList());
   }

}
