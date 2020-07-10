package com.kakarote.crm9.erp.crm.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.rocketmq.shade.io.netty.util.internal.StringUtil;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmReduceReasonsEnum;
import com.kakarote.crm9.erp.crm.common.CrmSalesOperationTypeEnum;
import com.kakarote.crm9.erp.crm.entity.DistributorBdReport;
import com.kakarote.crm9.erp.crm.entity.DistributorBdReportResult;
import com.kakarote.crm9.erp.crm.entity.DistributorBdSalesQuantityReport;
import com.kakarote.crm9.erp.crm.entity.DistributorBdSalesQuantityReportResult;
import com.kakarote.crm9.erp.crm.entity.DistributorSaleAreaReport;
import com.kakarote.crm9.erp.crm.entity.DistributorSaleAreaReport.OrderBy;
import com.kakarote.crm9.erp.crm.entity.DistributorSaleAreaReportResult;
import com.kakarote.crm9.erp.crm.entity.base.BaseDistributorStatistic;

/**
 * 分销商报表
 * @author xiaowen.wu
 *
 */
public class CrmDistributorReportService {

	@Inject
	CrmCustomerService crmCustomerService;
	
	/**
	 * 各省市硬件商品分销信息报表
	 * 
	 * @param basePageRequest
	 * @param userId
	 * @param isPage
	 * @return
	 */
	public Page<DistributorSaleAreaReportResult> queryDistributorSaleAreaReportList(BasePageRequest<DistributorSaleAreaReport> basePageRequest,Long userId,boolean isPage) {
		
		DistributorSaleAreaReport request = basePageRequest.getData();
		
		List<Long> longs= Aop.get(AdminUserService.class).queryUserByAuth(userId);//查询数据权限。 TODO

        Page<Record> paginate = new Page<Record>();
        Kv kv = Kv.by("saleAreaName", request.getSaleAreaName());
        kv.set("productName", request.getProductName());
        kv.set("productCode", request.getProductCode());
        kv.set("saleAreaCode", request.getSaleAreaCode());
        kv.set("deptId", request.getDeptId());
		// 组装排序条件
		StringBuilder orderByBuilder = new StringBuilder();
		List<OrderBy> orderBys = request.getOrderBys();
		StringBuilder orderStr = new StringBuilder();
		boolean hasOrder = false; //是否有效的排序条件
		if (CollectionUtils.isNotEmpty(orderBys)) {
			String[] orderStrs = new String[orderBys.size()];
			for (int i=0; i<orderBys.size();i++) {
				OrderBy orderBy = orderBys.get(i);
				orderByBuilder.delete(0, orderByBuilder.length());
 				if (Objects.nonNull(orderBy.getOrderKey()) && Objects.nonNull(orderBy.getOrderType())) {
					orderByBuilder.append(orderBy.getOrderKey().getCode()).append(' ').append(orderBy.getOrderType().getCode());
					orderStrs[i] = orderByBuilder.toString();
	 				hasOrder = true;
 				}
 			}
 			if (hasOrder) {
 	 			orderStr.append(" order by ").append(String.join(",", orderStrs));
 			}
		} 
		if (!hasOrder){
			// 默认排序
			orderStr.append(" order by deptName, saleAreaName");
		}
        kv.set("orderBys",orderStr.toString());
        SqlPara sqlPara = Db.getSqlPara("crm.distributorReport.queryDistributorSaleAreaReportList", kv);
        if(isPage){
//            paginate = Db.paginateByFullSql(basePageRequest.getPage(), basePageRequest.getLimit(),true,sqlPara.getSql(),sqlPara.getSql()+" order by "+orderStr.toString(),sqlPara.getPara());
            paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),sqlPara);
        }else{
            paginate.setList(Db.find(sqlPara));
        }
        
        // 补充销售数量+计算库存
		return this.caculateInventory(paginate, basePageRequest);
	}
	
	/**
	 * 补充销售数量+计算库存
	 * @param paginate
	 * @return
	 */
	private Page<DistributorSaleAreaReportResult> caculateInventory(Page<Record> paginate, BasePageRequest<DistributorSaleAreaReport> basePageRequest) {
		
		// 申明返回值
		Page<DistributorSaleAreaReportResult> result = new Page<DistributorSaleAreaReportResult>();
		result.setPageNumber(paginate.getPageNumber());
		result.setPageSize(paginate.getPageSize());
		result.setTotalPage(paginate.getTotalPage());
		result.setTotalRow(paginate.getTotalRow());
		
//		// 获取bd销售数量
//        Kv kv = Kv.by("saleAreaName", basePageRequest.getData().getSaleAreaName());
//        kv.set("productName", basePageRequest.getData().getProductName());
//        SqlPara sqlPara = Db.getSqlPara("crm.distributorReport.queryDistributorRelation", kv);
//        List<Record> relationRecords = Db.find(sqlPara);
//        List<Integer> customerIds = relationRecords.stream().map(record -> record.getInt("customerId")).collect(Collectors.toList());
//		List<DistributorBdSalesStatistic> distributorBdSalesStatistics = crmCustomerService.getBdSalesStatisticInfoByCustomerIds(customerIds);
		
		// 填充库存及销售数量
		List<DistributorSaleAreaReportResult> distributorSaleAreaReportResults = paginate.getList().stream().map(record -> {
			
//			// bd销售数量
//			// 添加分类条件商品code+商品规格
//			BigDecimal bdSalesQuantity = distributorBdSalesStatistics.stream().filter(distributorBdSalesStatistic -> 
//				relationRecords.stream().filter(relationRecord -> relationRecord.getStr("saleAreaCode").equals(record.getStr("saleAreaCode"))
//						&& relationRecord.getStr("productCode").equals(record.getStr("productCode"))
//						&& relationRecord.getStr("goodsSpec").equals(record.getStr("goodsSpec")))
//				.anyMatch(relationRecord -> relationRecord.getStr("customerId").equals(distributorBdSalesStatistic.getCrmCustomerId())
//						&& relationRecord.getStr("siteMemberId").equals(String.valueOf(distributorBdSalesStatistic.getSiteMemberId()))
//						&& relationRecord.getStr("goodsSpec").equals(distributorBdSalesStatistic.getGoodsSpec())
//						&& relationRecord.getStr("productCode").equals(distributorBdSalesStatistic.getProductCode()))
//			).map(distributorBdSalesStatistic -> new BigDecimal(distributorBdSalesStatistic.getSalesQuantityValue()))
//			.reduce(BigDecimal.ZERO,BigDecimal::add);
//			// 订购数量
//			Long sumSalesQuantity = record.getLong("sumSalesQuantity");
//			// 已发货数量
//			Long sumDeliveryQuantity = record.getLong("sumDeliveryQuantity");
//			
//			DistributorSaleAreaReportResult distributorSaleAreaReportResult = new DistributorSaleAreaReportResult();
//			distributorSaleAreaReportResult.setActualInventory(new BigDecimal(sumDeliveryQuantity).subtract(bdSalesQuantity).longValue());
//			distributorSaleAreaReportResult.setBdSalesQuantity(Long.valueOf(bdSalesQuantity.longValue()));
//			distributorSaleAreaReportResult.setGoodsSpec(record.getStr("goodsSpec"));
//			distributorSaleAreaReportResult.setProductName(record.getStr("productName"));
//			distributorSaleAreaReportResult.setSaleAreaCode(record.getStr("saleAreaCode"));
//			distributorSaleAreaReportResult.setSaleAreaName(StringUtil.isNullOrEmpty(record.getStr("saleAreaName"))?"省份未知":record.getStr("saleAreaName"));
//			distributorSaleAreaReportResult.setSumDeliveryQuantity(sumDeliveryQuantity);
//			distributorSaleAreaReportResult.setSumSalesQuantity(sumSalesQuantity);
//			distributorSaleAreaReportResult.setTheoreticalInventory(new BigDecimal(sumSalesQuantity).subtract(bdSalesQuantity).longValue());

			DistributorSaleAreaReportResult distributorSaleAreaReportResult = new DistributorSaleAreaReportResult();
			distributorSaleAreaReportResult.setActualInventory(Objects.nonNull(record.getLong("actualInventory")) ? record.getLong("actualInventory") : 0L);
			distributorSaleAreaReportResult.setBdSalesQuantity(Objects.nonNull(record.getLong("bdSalesQuantity")) ? record.getLong("bdSalesQuantity") : 0L);
			distributorSaleAreaReportResult.setGoodsSpec(record.getStr("goodsSpec"));
			distributorSaleAreaReportResult.setProductName(record.getStr("productName"));
			distributorSaleAreaReportResult.setSaleAreaCode(record.getStr("saleAreaCode"));
			distributorSaleAreaReportResult.setSaleAreaName(StringUtil.isNullOrEmpty(record.getStr("saleAreaName"))?"省份未知":record.getStr("saleAreaName"));
			distributorSaleAreaReportResult.setSumDeliveryQuantity(Objects.nonNull(record.getLong("sumDeliveryQuantity")) ? record.getLong("sumDeliveryQuantity") : 0L);
			distributorSaleAreaReportResult.setSumSalesQuantity(Objects.nonNull(record.getLong("sumSalesQuantity")) ? record.getLong("sumSalesQuantity") : 0L);
			distributorSaleAreaReportResult.setTheoreticalInventory(Objects.nonNull(record.getLong("theoreticalInventory")) ? record.getLong("theoreticalInventory") : 0L);
			distributorSaleAreaReportResult.setDeptId(record.getLong("deptId"));
			distributorSaleAreaReportResult.setDeptName(record.getStr("deptName"));
			return distributorSaleAreaReportResult;
		}).collect(Collectors.toList());
		
		result.setList(distributorSaleAreaReportResults);
		return result;
	}
	
	/**
	 * 我的分销商的分销信息报表
	 * 
	 * @param basePageRequest
	 * @param userId
	 * @param isPage
	 * @return
	 */
	public Page<DistributorBdReportResult> queryDistributorBdReportList(BasePageRequest<DistributorBdReport> basePageRequest,Long userId,boolean isPage) {
		
		DistributorBdReport request = basePageRequest.getData();
		
		List<Long> longs= Aop.get(AdminUserService.class).queryUserByAuth(userId);//查询数据权限。
        Page<Record> paginate = new Page<Record>();
        Kv kv = Kv.by("saleAreaName", request.getSaleAreaName());
        kv.set("productName", request.getProductName());
        kv.set("userIds", longs);
        kv.set("productCode", request.getProductCode());
        kv.set("saleAreaCode", request.getSaleAreaCode());
        kv.set("deptId", request.getDeptId());
        kv.set("customerId", request.getCustomerId());
        kv.set("bdCode", request.getBdCode());
        kv.set("bdName", request.getBdName());
		// 组装排序条件
		StringBuilder orderByBuilder = new StringBuilder();
		List<com.kakarote.crm9.erp.crm.entity.DistributorBdReport.OrderBy> orderBys = request.getOrderBys();
		StringBuilder orderStr = new StringBuilder();
		boolean hasOrder = false; //是否有效的排序条件
		if (CollectionUtils.isNotEmpty(orderBys)) {
			String[] orderStrs = new String[orderBys.size()];
			for (int i=0; i<orderBys.size();i++) {
				com.kakarote.crm9.erp.crm.entity.DistributorBdReport.OrderBy orderBy = orderBys.get(i);
				orderByBuilder.delete(0, orderByBuilder.length());
 				if (Objects.nonNull(orderBy.getOrderKey()) && Objects.nonNull(orderBy.getOrderType())) {
					orderByBuilder.append(orderBy.getOrderKey().getCode()).append(' ').append(orderBy.getOrderType().getCode());
					orderStrs[i] = orderByBuilder.toString();
	 				hasOrder = true;
 				}
 			}
 			if (hasOrder) {
 	 			orderStr.append(" order by ").append(String.join(",", orderStrs));
 			}
		} 
		if (!hasOrder){
			// 默认排序
			orderStr.append(" order by deptName, saleAreaName, bdName");
		}
        kv.set("orderBys",orderStr.toString());
        SqlPara sqlPara = Db.getSqlPara("crm.distributorReport.queryDistributorBdReportList", kv);
        if(isPage){
            paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),sqlPara);
        }else{
            paginate.setList(Db.find(sqlPara));
        }
        
        // 补充销售数量+计算库存
		return this.caculateBdInventory(paginate, basePageRequest);
	}
	
	/**
	 * 补充bd分销信息 销售数量+计算库存
	 * @param paginate
	 * @return
	 */
	private Page<DistributorBdReportResult> caculateBdInventory(Page<Record> paginate, BasePageRequest<DistributorBdReport> basePageRequest) {
		
		// 申明返回值
		Page<DistributorBdReportResult> result = new Page<DistributorBdReportResult>();
		result.setPageNumber(paginate.getPageNumber());
		result.setPageSize(paginate.getPageSize());
		result.setTotalPage(paginate.getTotalPage());
		result.setTotalRow(paginate.getTotalRow());
		
//		// 获取bd销售数量
//		List<Record> relationRecords = paginate.getList();
//        List<Integer> customerIds = relationRecords.stream().map(record -> record.getInt("customerId")).collect(Collectors.toList());
//		List<DistributorBdSalesStatistic> distributorBdSalesStatistics = crmCustomerService.getBdSalesStatisticInfoByCustomerIds(customerIds);
		
		// 填充库存及销售数量
		List<DistributorBdReportResult> distributorBdReportResults = paginate.getList().stream().map(record -> {
			
//			// bd销售数量
//			int bdSalesQuantityInt = distributorBdSalesStatistics.stream().filter(distributorBdSalesStatistic -> record.getStr("customerId").equals(distributorBdSalesStatistic.getCrmCustomerId())
//					&& record.getStr("siteMemberId").equals(String.valueOf(distributorBdSalesStatistic.getSiteMemberId()))
//					&& record.getStr("goodsSpec").equals(distributorBdSalesStatistic.getGoodsSpec())
//					&& record.getStr("productCode").equals(distributorBdSalesStatistic.getProductCode())).findFirst().orElseGet(() -> new DistributorBdSalesStatistic(null,0,null,null,null,null,0)).getSalesQuantityValue();
//			BigDecimal bdSalesQuantity = new BigDecimal(bdSalesQuantityInt);
//			// 订购数量
//			Long sumSalesQuantity = record.getLong("sumSalesQuantity");
//			// 已发货数量
//			Long sumDeliveryQuantity = record.getLong("sumDeliveryQuantity");
//			
//			DistributorBdReportResult distributorBdReportResult = new DistributorBdReportResult();
//			distributorBdReportResult.setActualInventory(new BigDecimal(sumDeliveryQuantity).subtract(bdSalesQuantity).longValue());
//			distributorBdReportResult.setBdSalesQuantity(Long.valueOf(bdSalesQuantity.longValue()));
//			distributorBdReportResult.setGoodsSpec(record.getStr("goodsSpec"));
//			distributorBdReportResult.setProductName(record.getStr("productName"));
//			distributorBdReportResult.setSaleAreaCode(record.getStr("saleAreaCode"));
//			distributorBdReportResult.setSaleAreaName(StringUtil.isNullOrEmpty(record.getStr("saleAreaName"))?"省份未知":record.getStr("saleAreaName"));
//			distributorBdReportResult.setSumDeliveryQuantity(sumDeliveryQuantity);
//			distributorBdReportResult.setSumSalesQuantity(sumSalesQuantity);
//			distributorBdReportResult.setTheoreticalInventory(new BigDecimal(sumSalesQuantity).subtract(bdSalesQuantity).longValue());
//			distributorBdReportResult.setCustomerName(StringUtil.isNullOrEmpty(record.getStr("customerName"))?"BD未知":record.getStr("customerName"));
//			distributorBdReportResult.setBdName(record.getStr("bdName"));
//			distributorBdReportResult.setBdCode(record.getStr("bdCode"));
			
			DistributorBdReportResult distributorBdReportResult = new DistributorBdReportResult();
			distributorBdReportResult.setActualInventory(Objects.nonNull(record.getLong("actualInventory")) ? record.getLong("actualInventory") : 0L);
			distributorBdReportResult.setBdSalesQuantity(Objects.nonNull(record.getLong("bdSalesQuantity")) ? record.getLong("bdSalesQuantity") : 0L);
			distributorBdReportResult.setGoodsSpec(record.getStr("goodsSpec"));
			distributorBdReportResult.setProductName(record.getStr("productName"));
			distributorBdReportResult.setSaleAreaCode(record.getStr("saleAreaCode"));
			distributorBdReportResult.setSaleAreaName(StringUtil.isNullOrEmpty(record.getStr("saleAreaName"))?"省份未知":record.getStr("saleAreaName"));
			distributorBdReportResult.setSumDeliveryQuantity(Objects.nonNull(record.getLong("sumDeliveryQuantity")) ? record.getLong("sumDeliveryQuantity") : 0L);
			distributorBdReportResult.setSumSalesQuantity(Objects.nonNull(record.getLong("sumSalesQuantity")) ? record.getLong("sumSalesQuantity") : 0L);
			distributorBdReportResult.setTheoreticalInventory(Objects.nonNull(record.getLong("theoreticalInventory")) ? record.getLong("theoreticalInventory") : 0L);
			distributorBdReportResult.setCustomerName(StringUtil.isNullOrEmpty(record.getStr("customerName"))?"BD未知":record.getStr("customerName"));
			distributorBdReportResult.setBdName(StringUtil.isNullOrEmpty(record.getStr("bdName"))?"-":record.getStr("bdName"));
			distributorBdReportResult.setBdCode(record.getStr("bdCode"));
			distributorBdReportResult.setDeptId(record.getLong("deptId"));
			distributorBdReportResult.setDeptName(record.getStr("deptName"));
			return distributorBdReportResult;
		}).collect(Collectors.toList());
		
		result.setList(distributorBdReportResults);
		return result;
	}
	
	/**
	 * 销售数量报表
	 * 
	 * @param basePageRequest
	 * @param userId
	 * @param isPage
	 * @return
	 */
	public Page<DistributorBdSalesQuantityReportResult> queryDistributorBdSalesQuantityReportList(BasePageRequest<DistributorBdSalesQuantityReport> basePageRequest,Long userId,boolean isPage) {
		
		DistributorBdSalesQuantityReport request = basePageRequest.getData();
		
		List<Long> longs= Aop.get(AdminUserService.class).queryUserByAuth(userId);//查询数据权限。
        Page<Record> paginate = new Page<Record>();
        Kv kv = Kv.by("saleAreaName", request.getSaleAreaName());
        kv.set("productName", request.getProductName());
        kv.set("saleAreaCode", request.getSaleAreaCode());
        kv.set("customerName", request.getCustomerName());
        kv.set("bdCode", request.getBdCode());
        kv.set("deptId", request.getDeptId());
        kv.set("customerId", request.getCustomerId());
        kv.set("productCode", request.getProductCode());
        kv.set("operationType", request.getOperationType());
        kv.set("salesStartTime", request.getSalesStartTime());
        kv.set("salesEndTime", request.getSalesEndTime());
        kv.set("userIds", longs);
        // 组装排序条件
 		StringBuilder orderByBuilder = new StringBuilder();
 		List<com.kakarote.crm9.erp.crm.entity.DistributorBdSalesQuantityReport.OrderBy> orderBys = request.getOrderBys();
 		StringBuilder orderStr = new StringBuilder();
		boolean hasOrder = false; //是否有效的排序条件
 		if (CollectionUtils.isNotEmpty(orderBys)) {
 			String[] orderStrs = new String[orderBys.size()];
 			for (int i=0; i<orderBys.size();i++) {
 				com.kakarote.crm9.erp.crm.entity.DistributorBdSalesQuantityReport.OrderBy orderBy = orderBys.get(i);
 				orderByBuilder.delete(0, orderByBuilder.length());
 				if (Objects.nonNull(orderBy.getOrderKey()) && Objects.nonNull(orderBy.getOrderType())) {
	 				orderByBuilder.append(orderBy.getOrderKey().getCode()).append(' ').append(orderBy.getOrderType().getCode());
	 				orderStrs[i] = orderByBuilder.toString();
	 				hasOrder = true;
 				}
 			}
 			if (hasOrder) {
 	 			orderStr.append(" order by ").append(String.join(",", orderStrs));
 			}
 		} 
 		if (!hasOrder) {
 			// 默认排序
 			orderStr.append(" order by admin_user.realname desc,customer.customer_name,sales_log.product_name,sales_log.goods_spec,sales_log.gmt_create desc");
 		}
         kv.set("orderBys",orderStr.toString());
        SqlPara sqlPara = Db.getSqlPara("crm.distributorReport.queryDistributorBdSalesQuantityReportList", kv);
        if(isPage){
            paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),sqlPara);
        }else{
            paginate.setList(Db.find(sqlPara));
        }
        
        Page<DistributorBdSalesQuantityReportResult> result = new Page<DistributorBdSalesQuantityReportResult>();
		result.setPageNumber(paginate.getPageNumber());
		result.setPageSize(paginate.getPageSize());
		result.setTotalPage(paginate.getTotalPage());
		result.setTotalRow(paginate.getTotalRow());
		result.setList(paginate.getList().stream().map(record -> {
			DistributorBdSalesQuantityReportResult reportResult = new DistributorBdSalesQuantityReportResult();
			reportResult.setBdCode(record.getStr("bdCode"));
			reportResult.setBdName(record.getStr("bdName"));
			reportResult.setBdSalesQuantity(record.getLong("bdSalesQuantity"));
			reportResult.setCustomerId(record.getStr("customerId"));
			reportResult.setCustomerName(record.getStr("customerName"));
			reportResult.setGmtCreate(record.getDate("gmtCreate"));
			reportResult.setGoodsSpec(record.getStr("goodsSpec"));
			reportResult.setOperationType(record.getInt("operationType"));
			reportResult.setOperationTypeName(CrmSalesOperationTypeEnum.getTypeByCode(record.getInt("operationType")));
			reportResult.setProductName(record.getStr("productName"));
			reportResult.setSaleAreaCode(record.getStr("saleAreaCode"));
			reportResult.setSaleAreaName(StringUtil.isNullOrEmpty(record.getStr("saleAreaName"))?"省份未知":record.getStr("saleAreaName"));
			if (Objects.nonNull(record.getInt("reduceReasons"))) {
				reportResult.setReduceReasons(record.getInt("reduceReasons"));
				reportResult.setReduceReasonsName(CrmReduceReasonsEnum.getName(record.getInt("reduceReasons")));
			}
			reportResult.setRemark(record.getStr("remark"));
			return reportResult;
		}).collect(Collectors.toList()));
		
		return result;
	}
	
	public Page<Record> queryDistributorProductList(String name, Long userId,boolean isPage) {
		
		Page<Record> paginate = new Page<Record>();
		paginate.setList(Db.find(Db.getSqlPara("crm.distributorReport.queryDistributorProductList", Kv.by("productName", name))));
		return paginate;
	}
	
	public Page<Record> queryDistributorList(String name, Long userId,boolean isPage) {
		
		Page<Record> paginate = new Page<Record>();
		paginate.setList(Db.find(Db.getSqlPara("crm.distributorReport.queryDistributorList", Kv.by("customerName", name))));
		return paginate;
	}
	
}
