package com.kakarote.crm9.integration.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.shade.io.netty.util.internal.StringUtil;
import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.config.cache.RedisCache;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmCustomerChangeLogEnum;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.service.CrmChangeLogService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmRecordService;
import com.kakarote.crm9.erp.crm.service.CrmSiteMemberService;
import com.kakarote.crm9.integration.entity.SpecialCustomerAllocateRuleDto;
import com.kakarote.crm9.integration.entity.leadsallocation.AllocationTarget;
import com.kakarote.crm9.integration.entity.leadsallocation.LeadsSource;
import com.kakarote.crm9.integration.entity.leadsallocation.SourceTypeEnum;
import com.kakarote.crm9.integration.entity.leadsallocation.TargetTypeEnum;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TraceIdUtil;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 资源分发
 * @author xiaowen.wu
 */
public class LeadsAllocationService {

	/**
	 * 分发阈值
	 * 开发2 测试195 生产198
	 *
	 */
	public String threshold = JfinalConfig.crmProp.get("leadsAllocation.threshold");
//	public String threshold = "2";

	/**
	 * 分发对象 多个部门支持","分割
	 * 开发2 测试195 生产198
	 * 
	 */
	private String deptId = JfinalConfig.crmProp.get("leadsAllocation.deptId");
//	public String deptId = "2";

	private String test = JfinalConfig.crmProp.get("leadsAllocation.test");
//	public String test = "true";

	private String operator = JfinalConfig.crmProp.get("leadsAllocation.operator");
//	public String operator = "-1";

	private String excludeReason = JfinalConfig.crmProp.get("leadsAllocation.excludeReason");
//	public String excludeReason = "-1";
	
	private AdminUserService adminUserService = Aop.get(AdminUserService.class);

	private CrmRecordService crmRecordService = Aop.get(CrmRecordService.class);

	private CrmCustomerService crmCustomerService = Aop.get(CrmCustomerService.class);

	private static final String ALLOCATION_TARGET_DAY_USER_ID = "allocation_target:day:userId:";

	private final String specialCustomerAllocateRule = JfinalConfig.crmProp.get("special.customer.allocate.rule");

	@Inject
	private RedisCache redisCache;
	@Inject
	private CrmChangeLogService crmChangeLogService;
	@Inject
	private CrmSiteMemberService crmSiteMemberService;


	/**
	 * 可分发的总数量
	 */
	private long dataLimit;

	/**
	 * 资源分发
	 */
    public R leadsAllocation() {
    	// 1、获取对象
    	List<AllocationTarget> targets = this.getTarget();

    	// 2、获取资源
    	LeadsSource<Record> customers = getSource(dataLimit);

		// 3、分发
		allocation(targets, customers);
    	
    	return R.ok();
    }
    
    /**
     * 获取资源
     * @param dataLimit d
     */
	private LeadsSource<Record> getSource(long dataLimit) {
		//获取不参与分发的会员注册渠道
		List<SpecialCustomerAllocateRuleDto> allocateRuleDtos = JSONObject.parseArray(specialCustomerAllocateRule, SpecialCustomerAllocateRuleDto.class);
		List<String> channels = Lists.newArrayList();
		if (CollectionUtils.isNotEmpty(allocateRuleDtos)) {
			channels.addAll(allocateRuleDtos.stream().map(SpecialCustomerAllocateRuleDto::getMemberChannel).collect(Collectors.toList()));
		}
		Kv kv = Kv.by("dataLimit", dataLimit).set("relatedType", CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
		kv.set("channels", channels);
		if (!StringUtil.isNullOrEmpty(excludeReason)) {
			kv.set("excludeReason", Arrays.stream(excludeReason.split(",")).map(Long::valueOf).collect(Collectors.toList()));
		}

		List<Record> r = Db.find(Db.getSqlPara("crm.customer.queryCustomerLeads", kv));

		LeadsSource<Record> src = new LeadsSource<>();
		src.setSourceType(SourceTypeEnum.CUSTOMER);
		src.setSources(r);
		return src;
	}
    
    /**
     * 获取分发对象
     */
    private List<AllocationTarget> getTarget() {

    	List<Record> recordList = adminUserService.queryUserByDeptIdEliminateDisabledUser(Integer.valueOf(deptId));
    	List<AllocationTarget> allocationTargets =
    	recordList.stream().map(record -> {
    		// 用户id
    		Long userId = record.getLong("id");
        	AllocationTarget allocationTarget = new AllocationTarget();
        	allocationTarget.setTargetId(userId);
        	allocationTarget.setTargetName(record.getStr("realname"));
        	allocationTarget.setTargetType(TargetTypeEnum.USER);

			long onceThreshold = record.getLong("once_threshold");
			long dayThreshold = record.getLong("day_threshold");
			Long userThreshold = redisCache.get(ALLOCATION_TARGET_DAY_USER_ID + record.getLong("id"));
			if (userThreshold != null){
				dayThreshold = userThreshold;
			}
			allocationTarget.setOnceThreshold(onceThreshold);
			allocationTarget.setDayThreshold(dayThreshold);
    		// 剩余库存（非负整数） -1表示没有库存限制
    		long surplusCapacity = this.alculatCapacity(userId);
			// 添加库存限制
			allocationTarget.setUsefulThreshold(surplusCapacity==-1 ? Math.min(onceThreshold, dayThreshold) : Math.min(surplusCapacity, Math.min(onceThreshold, dayThreshold)));

			dataLimit += allocationTarget.getUsefulThreshold();

        	return allocationTarget;
    	}).filter(o -> o.getUsefulThreshold() > 0).collect(Collectors.toList());
    	
    	if ("true".equals(test)) {
        	allocationTargets.clear();
        	allocationTargets.add(new AllocationTarget(TargetTypeEnum.USER,111L,"刘志祥",Long.valueOf(threshold),Long.valueOf(threshold),Long.valueOf(threshold)));
        	allocationTargets.add(new AllocationTarget(TargetTypeEnum.USER,1368L,"吴晓闻",Long.valueOf(threshold),Long.valueOf(threshold),Long.valueOf(threshold)));
        	allocationTargets.add(new AllocationTarget(TargetTypeEnum.USER,1351L,"张勤耘",Long.valueOf(threshold),Long.valueOf(threshold),Long.valueOf(threshold)));
        	allocationTargets.add(new AllocationTarget(TargetTypeEnum.USER,1217L,"张海泉",Long.valueOf(threshold),Long.valueOf(threshold),Long.valueOf(threshold)));
    	}
    	return allocationTargets;
    }

	/**
	 * 轮询分发处理
	 * @param targets t
	 * @param customers c
	 */
	private void allocation(List<AllocationTarget> targets,LeadsSource<Record> customers) {
		Date currentDate = new Date();

		Db.tx(() -> {
			int index = -1;
			AllocationTarget allocationTarget;
			List<CrmCustomer> crmCustomers = new ArrayList<>();
	
			for (Record record : customers.getSources()) {
				if (targets.size() == 0){
					break;
				}
	
				CrmCustomer oldCrmCustomer = new CrmCustomer()._setAttrs(record.getColumns());
				CrmCustomer crmCustomer = new CrmCustomer()._setAttrs(record.getColumns());
				int nextIndex = (index+1) % targets.size();
				allocationTarget = targets.get(nextIndex);
	
				allocationTarget.setUsefulThreshold(allocationTarget.getUsefulThreshold() - 1);
				allocationTarget.setDayThreshold(allocationTarget.getDayThreshold() - 1);
				if (allocationTarget.getUsefulThreshold() == 0){
					redisCache.put(ALLOCATION_TARGET_DAY_USER_ID + allocationTarget.getTargetId(), allocationTarget.getDayThreshold(), (int)DateUtil.between(currentDate,DateUtil.endOfDay(currentDate), DateUnit.SECOND), TimeUnit.SECONDS);
					targets.remove(nextIndex);
				}else {
					index = nextIndex;
				}
	
				crmCustomer.setOwnerUserId(allocationTarget.getTargetId().intValue());
				crmCustomer.setOwnerUserName(allocationTarget.getTargetName());
				crmCustomer.setOwnerTime(DateUtil.date());
				crmCustomer.setUpdateTime(currentDate);
				crmCustomers.add(crmCustomer);
	
				// 记录分发日志
				crmRecordService.updateRecord(oldCrmCustomer,crmCustomer, CrmEnum.CUSTOMER_DISTRIBUTE_KEY.getTypes(),"-1".equals(operator)? null : Long.valueOf(operator));
				
				// 记录库存类型(默认分发客户进入考察库)
				crmCustomerService.saveCrmCustomerExt(crmCustomer.getCustomerId(), CustomerStorageTypeEnum.INSPECT_CAP.getCode(),null,null);
	
				//记录客户变更日志
				crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.getBdByStorageType(CustomerStorageTypeEnum.INSPECT_CAP.getCode()), crmCustomer.getCustomerId(), Long.valueOf(crmCustomer.getOwnerUserId()), null, null);
	
			}
			TraceIdUtil.remove();
			// 轮询分发
			if (CollectionUtils.isNotEmpty(crmCustomers)) {
				Db.batchUpdate(crmCustomers, crmCustomers.size());
			}
	
			//更新阈值
			targets.forEach(o -> redisCache.put(ALLOCATION_TARGET_DAY_USER_ID + o.getTargetId(), o.getDayThreshold(), (int)DateUtil.between(currentDate,DateUtil.endOfDay(currentDate), DateUnit.SECOND), TimeUnit.SECONDS));
			return true;
		});
    }

    /**
     * 计算库存
     * 剩余库存（非负整数） -1表示没有库存限制
     */
    private long alculatCapacity(Long userId) {
    	long surplusCapacity = -1;
		Record capacityRecord = crmCustomerService.searchUserCapacity(userId);
		if(Objects.nonNull(capacityRecord) && Objects.nonNull(capacityRecord.getLong("inspect_cap"))) {
			// 总库存
			BigDecimal inspectCap = BigDecimal.valueOf(capacityRecord.getLong("inspect_cap"));
			// 已用库存
			BigDecimal usedInspectCap = BigDecimal.valueOf(capacityRecord.getLong("used_inspect_cap"));
			surplusCapacity = inspectCap.compareTo(usedInspectCap) < 0 ? 0L : inspectCap.subtract(usedInspectCap).longValue();
		}
		return surplusCapacity;
    }

}
