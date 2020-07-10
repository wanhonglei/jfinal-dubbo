package com.kakarote.crm9.erp.crm.service;

import com.google.common.collect.Maps;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.entity.CrmProductReceivablesReport;
import com.kakarote.crm9.erp.crm.entity.CrmProductReceivablesReportResult;
import com.kakarote.crm9.utils.ReportUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 回款报表服务类
 * @author honglei.wan
 */
public class CrmProductReceivablesReportService {

    @Inject
    private AdminDeptService adminDeptService;
    
	public CrmProductReceivablesReportResult queryProductReceivablesReportList(BasePageRequest<CrmProductReceivablesReport> basePageRequest,Long userId,boolean isPage) {
		
		CrmProductReceivablesReport request = basePageRequest.getData();
		CrmProductReceivablesReportResult result = new CrmProductReceivablesReportResult();
		
		List<Long> longs= Aop.get(AdminUserService.class).queryUserByAuth(userId);//查询数据权限。

//		List<Integer> deptIds = request.getDeptId()!=null && !"".equals(request.getDeptId()) ? 
//				adminDeptService.getDeptAndBranchDept(Integer.valueOf(request.getDeptId())) :
//					new ArrayList<Integer>();
		Page<Record> paginate = new Page<>();
        Kv kv = Kv.by("deptId", request.getDeptId());
        kv.set("categoryId", request.getCategoryId())
        .set("receivablesPlanStartTime", request.getReceivablesPlanStartTime())
        .set("receivablesPlanEndTime", request.getReceivablesPlanEndTime())
        .set("winRate",request.getWinRate())
        .set("receivablesStartTime",request.getReceivablesStartTime())
        .set("receivablesEndTime",request.getReceivablesEndTime())
        .set("owner_user_id",longs)
        .set("scenarioId",request.getScenarioId());
        SqlPara sqlPara = Db.getSqlPara("crm.receivables.queryProductReceivablesReportList", kv);
        if(isPage){
            paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),sqlPara);
        }else{
            paginate.setList(Db.find(sqlPara));
        }

        // 计算总计
        Page<Record> allPaginate = new Page<>();
        allPaginate.setList(Db.find(sqlPara));
        
        result.setTotalReceivablesPlanMoney(ReportUtil.formatMoney(caculateSum(allPaginate, "receivablesPlanMoney", "planId")));
        result.setTotalReceivablesMoney(ReportUtil.formatMoney(caculateSum(allPaginate, "receivablesMoney", "receivablesId")));
        paginate.setList(caculateReceivablesMoney(paginate));// 分摊金额
        result.setPageResult(paginate);
        
		return result;
	}
	
	/**
	 *  分摊回款金额
	 * @param request
	 * @return
	 */
	private List<Record> caculateReceivablesMoney(Page<Record> request) {

		Map<String, Double> sumMoney = request.getList().stream().
				collect(Collectors.groupingBy(record -> record.getInt("planId") +"_"+ record.getInt("receivablesId"),
						Collectors.summingDouble(record -> formateBigdecimal(record,"salesPrice").doubleValue())));

		Map<String, List<Record>> groupRecord = request.getList().stream().collect(Collectors.groupingBy(record -> record.getInt("planId") +"_"+ record.getInt("receivablesId")));

		Map<String, List<Record>> result = Maps.newHashMapWithExpectedSize(groupRecord.size());
		groupRecord.keySet().stream().map(planIdAndReceivablesId -> {
			
			List<Record> records = groupRecord.get(planIdAndReceivablesId);
			Double sum = sumMoney.get(planIdAndReceivablesId);
			int count = records.size();
			BigDecimal currentMoney= BigDecimal.ZERO;
			BigDecimal currentPlanMoney= BigDecimal.ZERO;
			List<Record> resultRecords = new ArrayList<>(count);
			for (int i=0; i<count; i++) {
				
				Record resultRecord = new Record();
				resultRecord.set("productId", records.get(i).getInt("productId"));
				if(i<count-1) {
					BigDecimal receivablesMoney = BigDecimal.valueOf(0D).compareTo(BigDecimal.valueOf(sum)) == 0? BigDecimal.ZERO : formateBigdecimal(records.get(i),"receivablesMoney").multiply(formateBigdecimal(records.get(i),"salesPrice")).divide(new BigDecimal(sum),2,BigDecimal.ROUND_HALF_UP);
					BigDecimal receivablesPlanMoney = BigDecimal.valueOf(0D).compareTo(BigDecimal.valueOf(sum)) == 0? BigDecimal.ZERO : formateBigdecimal(records.get(i),"receivablesPlanMoney").multiply(formateBigdecimal(records.get(i),"salesPrice")).divide(new BigDecimal(sum),2,BigDecimal.ROUND_HALF_UP);
					resultRecord.set("receivablesMoney", "0.00".equals(ReportUtil.formatMoney(receivablesMoney)) ? "" : ReportUtil.formatMoney(receivablesMoney));
					resultRecord.set("receivablesPlanMoney", "0.00".equals(ReportUtil.formatMoney(receivablesPlanMoney)) ? "" :ReportUtil.formatMoney(receivablesPlanMoney));

					currentMoney = receivablesMoney.add(currentMoney);
					currentPlanMoney = receivablesPlanMoney.add(currentPlanMoney);
				} else {
					// 校准最后一笔分摊金额
					String receivablesMoney = ReportUtil.formatMoney(formateBigdecimal(records.get(i),"receivablesMoney").subtract(currentMoney));
					String receivablesPlanMoney = ReportUtil.formatMoney(formateBigdecimal(records.get(i),"receivablesPlanMoney").subtract(currentPlanMoney));
					resultRecord.set("receivablesMoney", "0.00".equals(receivablesMoney)? "": receivablesMoney);
					resultRecord.set("receivablesPlanMoney", "0.00".equals(receivablesPlanMoney) ? "" : receivablesPlanMoney);
				}
				resultRecords.add(resultRecord);
			}
			result.put(planIdAndReceivablesId, resultRecords);
			return result;
		}).collect(Collectors.toList());

		return request.getList().stream().peek(record -> result.keySet().forEach(planIdAndReceivablesId -> {

			if ((record.getInt("planId") +"_"+ record.getInt("receivablesId")).equals(planIdAndReceivablesId)) {
				Optional<Record> op = result.get(planIdAndReceivablesId).stream().filter(resultRecord -> Objects.nonNull(resultRecord.getInt("productId")) && resultRecord.getInt("productId").equals(record.getInt("productId"))).findFirst();
				Record nullRecord = new Record();
				nullRecord.set("receivablesMoney", 0);
				nullRecord.set("receivablesPlanMoney", 0);
				record.set("receivablesMoney", op.orElse(nullRecord).get("receivablesMoney"));
				record.set("receivablesPlanMoney", op.orElse(nullRecord).get("receivablesPlanMoney"));
			}
		})).collect(Collectors.toList());
	}
	
	private BigDecimal formateBigdecimal(Record record, String key) {
		
		return record.getBigDecimal(key)==null ? BigDecimal.ZERO:record.getBigDecimal(key);
	}
	
	/**
	 * 计算总计
	 * @param allPaginate
	 * @param key
	 * @return
	 */
	private BigDecimal caculateSum(Page<Record> allPaginate, String key, String groupKey) {
		
        Map<Integer, Double> sumReceivablesMap = allPaginate.getList().stream().filter(record -> record.getInt(groupKey)!=null).
        		collect(Collectors.groupingBy(record -> record.getInt(groupKey), 
        				Collectors.averagingDouble(record -> formateBigdecimal(record,key).doubleValue())));
        return sumReceivablesMap.values().stream().
        		map(receivablesMoney -> receivablesMoney==null ? BigDecimal.ZERO : new BigDecimal(receivablesMoney))
        		.reduce(BigDecimal.ZERO,BigDecimal::add);
	}
	
}
