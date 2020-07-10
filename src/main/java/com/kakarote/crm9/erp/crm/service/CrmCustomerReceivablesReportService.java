package com.kakarote.crm9.erp.crm.service;

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
import com.kakarote.crm9.erp.crm.entity.CustomerReceivablesReport;
import com.kakarote.crm9.erp.crm.entity.CustomerReceivablesReportResult;
import com.kakarote.crm9.utils.ReportUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户回款报表服务类
 * @author honglei.wan
 */
public class CrmCustomerReceivablesReportService {

    @Inject
    private AdminDeptService adminDeptService;

	public CustomerReceivablesReportResult queryCustomerReceivablesReportList(BasePageRequest<CustomerReceivablesReport> basePageRequest,Long userId,boolean isPage) {
		
		CustomerReceivablesReport request = basePageRequest.getData();
		CustomerReceivablesReportResult result = new CustomerReceivablesReportResult();
		//查询数据权限。
		List<Long> longs= Aop.get(AdminUserService.class).queryUserByAuth(userId);
        Page<Record> paginate = new Page<>();
        Kv kv = Kv.by("deptId", request.getDeptId());
        kv.set("customerName", request.getCustomerName())
        .set("receivablesPlanStartTime", request.getReceivablesPlanStartTime())
        .set("receivablesPlanEndTime", request.getReceivablesPlanEndTime())
        .set("winRate",request.getWinRate())
        .set("receivablesStartTime",request.getReceivablesStartTime())
        .set("receivablesEndTime",request.getReceivablesEndTime())
        .set("owner_user_id",longs)
        .set("ownerUserName",request.getOwnerUserName())
        .set("ownerUserId",request.getOwnerUserId());
        SqlPara sqlPara = Db.getSqlPara("crm.receivables.queryCustomerReceivablesReportList", kv);
        if(isPage){
            paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),sqlPara);
        }else{
            paginate.setList(Db.find(sqlPara));
        }
        Page<Record> allPaginate = new Page<>();
        allPaginate.setList(Db.find(sqlPara));
        result.setTotalReceivablesPlanMoney(ReportUtil.formatMoney(caculateSum(allPaginate, "receivablesPlanMoney", "planId")));
        result.setTotalReceivablesMoney(ReportUtil.formatMoney(caculateSum(allPaginate, "receivablesMoney", "receivablesId")));
        // 省市区格式化
        paginate.setList(formatAddress(paginate));
        result.setPageResult(paginate);
        
		return result;
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
	
	private BigDecimal formateBigdecimal(Record record, String key) {
		
		return record.getBigDecimal(key)==null ? BigDecimal.ZERO:record.getBigDecimal(key);
	}
	
	/**
	 * 省市区格式化
	 * @param paginate
	 * @return
	 */
	private List<Record> formatAddress(Page<Record> paginate) {
		
		return paginate.getList().stream().peek(record -> record.set("address", ReportUtil.formatAddress(record.get("address")))).collect(Collectors.toList());
	}
}
