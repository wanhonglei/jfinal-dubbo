package com.kakarote.crm9.erp.crm.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.crm.common.CrmDistributorBdReportEnum;
import com.kakarote.crm9.erp.crm.common.CrmDistributorBdSalesQuantityReportEnum;
import com.kakarote.crm9.erp.crm.common.CrmDistributorReportEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CommonCodeNameDTO;
import com.kakarote.crm9.erp.crm.entity.DistributorBdReport;
import com.kakarote.crm9.erp.crm.entity.DistributorBdReportResult;
import com.kakarote.crm9.erp.crm.entity.DistributorBdSalesQuantityReport;
import com.kakarote.crm9.erp.crm.entity.DistributorBdSalesQuantityReportResult;
import com.kakarote.crm9.erp.crm.entity.DistributorSaleAreaReport;
import com.kakarote.crm9.erp.crm.entity.DistributorSaleAreaReportResult;
import com.kakarote.crm9.erp.crm.service.CrmDistributorReportService;
import com.kakarote.crm9.integration.common.DistributorSaleAreaEnum;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.ExcelExportUtil;
import com.kakarote.crm9.utils.R;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分销商报表
 * @author xiaowen.wu
 *
 */
public class CrmDistributorReportController extends Controller {

	@Inject
	private CrmDistributorReportService crmDistributorReportService;
	
	@Inject
	private AdminDeptService adminDeptService;

	//开发195 测试195 生产294
	public String distributorBdDeptId = JfinalConfig.crmProp.get("distributor.report.deptId");
//	public String distributorBdDeptId = "195";
	// 剔除的部门id，","分割。开发：268,269 测试：268,269 生产：298,303,275,276
	public String distributorBdDeptIdExcutions= JfinalConfig.crmProp.get("distributor.report.deptId.excutions");
//	public String distributorBdDeptIdExcutions= "275,276";
	
    private Log logger = Log.getLog(getClass());

    /**
     * 各省市硬件商品分销信息报表
     * @param basePageRequest
     */
	@Permissions("board:distributorSaleAreaReport:view")
	public void queryDistributorSaleAreaReportList(BasePageRequest<DistributorSaleAreaReport> basePageRequest) {
		try {
			// 查询总计
			Page<DistributorSaleAreaReportResult> totalPage = crmDistributorReportService.queryDistributorSaleAreaReportList(basePageRequest, BaseUtil.getUserId(), false);
			Long totalSalesQuantity = totalPage.getList().stream().map(result -> Objects.nonNull(result.getSumSalesQuantity()) ? new BigDecimal(result.getSumSalesQuantity()) : BigDecimal.ZERO)
			.reduce(BigDecimal.ZERO, BigDecimal::add).longValue();
			Long totalDeliveryQuantity = totalPage.getList().stream().map(result -> Objects.nonNull(result.getSumDeliveryQuantity()) ? new BigDecimal(result.getSumDeliveryQuantity()) : BigDecimal.ZERO)
					.reduce(BigDecimal.ZERO, BigDecimal::add).longValue();
			Long totalBdSalesQuantity = totalPage.getList().stream().map(result -> Objects.nonNull(result.getBdSalesQuantity()) ? new BigDecimal(result.getBdSalesQuantity()) : BigDecimal.ZERO)
					.reduce(BigDecimal.ZERO, BigDecimal::add).longValue();
			// 组装分页查询
			renderJson(R.ok().put("data",new DistributorSaleAreaReportTotalResult(crmDistributorReportService.queryDistributorSaleAreaReportList(basePageRequest, BaseUtil.getUserId(), true)
					,totalSalesQuantity
					,totalDeliveryQuantity
					,totalBdSalesQuantity)));
		} catch (Exception e) {
			logger.error(String.format("queryDistributorSaleAreaReportList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
	}

	/**
	 * 各省市硬件商品分销信息报表导出
	 * @param basePageRequest
	 * @throws Exception
	 */
	@Permissions("board:distributorSaleAreaReport:export")
	public void exportDistributorSaleAreaReportExcel(BasePageRequest<DistributorSaleAreaReport> basePageRequest) throws Exception{
		Page<DistributorSaleAreaReportResult> page = crmDistributorReportService.queryDistributorSaleAreaReportList(basePageRequest, BaseUtil.getUserId(), false);
		List<LinkedHashMap<String,String>> headAllList = new ArrayList<>();
		List<List<Record>> resultAllList = new ArrayList<>();
		headAllList.add(initMain());
		resultAllList.add(page.getList().stream().map(distributorSaleAreaReportResult -> {
			Record record = new Record();
			record.set("deptName", distributorSaleAreaReportResult.getDeptName());
			record.set("saleAreaName", distributorSaleAreaReportResult.getSaleAreaName());
			record.set("productName", distributorSaleAreaReportResult.getProductName());
			record.set("goodsSpec", distributorSaleAreaReportResult.getGoodsSpec());
			record.set("sumSalesQuantity", distributorSaleAreaReportResult.getSumSalesQuantity());
			record.set("sumDeliveryQuantity", distributorSaleAreaReportResult.getSumDeliveryQuantity());
			record.set("bdSalesQuantity", distributorSaleAreaReportResult.getBdSalesQuantity());
			record.set("theoreticalInventory", distributorSaleAreaReportResult.getTheoreticalInventory());
			record.set("actualInventory", distributorSaleAreaReportResult.getActualInventory());
			return record;
		}).collect(Collectors.toList()));
		ExcelExportUtil.export(headAllList,resultAllList, CrmConstant.DISTRIBUTOR_SALE_AREA_REPORT,getResponse(),null);
		renderNull();
	}

	/**
	 * 各省市硬件商品分销信息报表导出head
	 * @return
	 */
	public LinkedHashMap<String,String> initMain() {
		LinkedHashMap<String,String> headList = new LinkedHashMap<>();
		for(CrmDistributorReportEnum crmDistributorReportEnum : CrmDistributorReportEnum.values()) {
			headList.put(crmDistributorReportEnum.getTypes(),crmDistributorReportEnum.getName());
		}
		return headList;
	}
	
    /**
     * 我的分销商分销信息报表
     * @param basePageRequest
     */
	@Permissions("board:distributorBdReport:view")
	public void queryDistributorBdReportList(BasePageRequest<DistributorBdReport> basePageRequest) {
		try {
			// 查询总计
			Page<DistributorBdReportResult> totalPage = crmDistributorReportService.queryDistributorBdReportList(basePageRequest, BaseUtil.getUserId(), false);
			Long totalSalesQuantity = totalPage.getList().stream().map(result -> Objects.nonNull(result.getSumSalesQuantity()) ? new BigDecimal(result.getSumSalesQuantity()) : BigDecimal.ZERO)
			.reduce(BigDecimal.ZERO, BigDecimal::add).longValue();
			Long totalDeliveryQuantity = totalPage.getList().stream().map(result -> Objects.nonNull(result.getSumDeliveryQuantity()) ? new BigDecimal(result.getSumDeliveryQuantity()) : BigDecimal.ZERO)
					.reduce(BigDecimal.ZERO, BigDecimal::add).longValue();
			Long totalBdSalesQuantity = totalPage.getList().stream().map(result -> Objects.nonNull(result.getBdSalesQuantity()) ? new BigDecimal(result.getBdSalesQuantity()) : BigDecimal.ZERO)
					.reduce(BigDecimal.ZERO, BigDecimal::add).longValue();
			// 组装分页查询
			renderJson(R.ok().put("data",new DistributorBdReportTotalResult(crmDistributorReportService.queryDistributorBdReportList(basePageRequest, BaseUtil.getUserId(), true)
					,totalSalesQuantity
					,totalDeliveryQuantity
					,totalBdSalesQuantity)));
		} catch (Exception e) {
			logger.error(String.format("queryDistributorBdReportList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
	}

	/**
	 * 我的分销商分销信息报表导出
	 * @param basePageRequest
	 * @throws Exception
	 */
	@Permissions("board:distributorBdReport:export")
	public void exportDistributorBdReportExcel(BasePageRequest<DistributorBdReport> basePageRequest) throws Exception{
		Page<DistributorBdReportResult> page = crmDistributorReportService.queryDistributorBdReportList(basePageRequest, BaseUtil.getUserId(), false);
		List<LinkedHashMap<String,String>> headAllList = new ArrayList<>();
		List<List<Record>> resultAllList = new ArrayList<>();
		headAllList.add(this.initMainBdReport());
		resultAllList.add(page.getList().stream().map(distributorBdReportResult -> {
			Record record = new Record();
			record.set("deptName", distributorBdReportResult.getDeptName());
			record.set("customerName", distributorBdReportResult.getCustomerName());
			record.set("saleAreaName", distributorBdReportResult.getSaleAreaName());
			record.set("bdName", distributorBdReportResult.getBdName());
			record.set("productName", distributorBdReportResult.getProductName());
			record.set("goodsSpec", distributorBdReportResult.getGoodsSpec());
			record.set("sumSalesQuantity", distributorBdReportResult.getSumSalesQuantity());
			record.set("sumDeliveryQuantity", distributorBdReportResult.getSumDeliveryQuantity());
			record.set("bdSalesQuantity", distributorBdReportResult.getBdSalesQuantity());
			record.set("theoreticalInventory", distributorBdReportResult.getTheoreticalInventory());
			record.set("actualInventory", distributorBdReportResult.getActualInventory());
			return record;
		}).collect(Collectors.toList()));
		ExcelExportUtil.export(headAllList,resultAllList, CrmConstant.DISTRIBUTOR_BD_REPORT,getResponse(),null);
		renderNull();
	}

	/**
	 * 我的分销商分销信息报表导出head
	 * @return
	 */
	private LinkedHashMap<String,String> initMainBdReport() {
		LinkedHashMap<String,String> headList = new LinkedHashMap<>();
		for(CrmDistributorBdReportEnum crmDistributorBdReportEnum : CrmDistributorBdReportEnum.values()) {
			headList.put(crmDistributorBdReportEnum.getTypes(),crmDistributorBdReportEnum.getName());
		}
		return headList;
	}
	
    /**
     * 销售数量报表
     * @param basePageRequest
     */
	@Permissions("board:distributorBdSalesQuantityReport:view")
	public void queryDistributorBdSalesQuantityReportList(BasePageRequest<DistributorBdSalesQuantityReport> basePageRequest) {
		try {
			// 按产品名称+商品规格分组汇总销售数量
			List<DistributorBdSalesQuantityReportResult> noPage = crmDistributorReportService.queryDistributorBdSalesQuantityReportList(basePageRequest, BaseUtil.getUserId(), false).getList();
			Map<String, LongSummaryStatistics> data = noPage.stream().collect(Collectors.groupingBy(o -> o.getProductName()+"_"+o.getGoodsSpec(),Collectors.summarizingLong(o -> o.getBdSalesQuantity())));
			List<ProductSum> productSums = new ArrayList<ProductSum>();
			for (Map.Entry<String, LongSummaryStatistics> entry : data.entrySet()) {
				String[] arr =entry.getKey().split("_");
				String productName = arr[0];
				String goodsSpec = arr[1];
				productSums.add(new ProductSum(productName, goodsSpec, entry.getValue().getSum()));
			}
			Map<String, Object> result = new HashMap<String,Object>();
			result.put("page", crmDistributorReportService.queryDistributorBdSalesQuantityReportList(basePageRequest, BaseUtil.getUserId(), true));
			result.put("productList", productSums.stream().sorted(Comparator.comparingLong(ProductSum::getNum).reversed()).collect(Collectors.toList()));//按汇总销售数量倒序
			renderJson(R.ok().put("data",result));
		} catch (Exception e) {
			logger.error(String.format("queryDistributorBdSalesQuantityReportList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ProductSum implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -9033587452776548907L;
		
		private String productName;
		private String goodsSpec;
		private long num;
	}

	/**
	 * 销售数量报表导出
	 * @param basePageRequest
	 * @throws Exception
	 */
	@Permissions("board:distributorBdSalesQuantityReport:export")
	public void exportDistributorBdSalesQuantityReportExcel(BasePageRequest<DistributorBdSalesQuantityReport> basePageRequest) throws Exception{
		Page<DistributorBdSalesQuantityReportResult> page = crmDistributorReportService.queryDistributorBdSalesQuantityReportList(basePageRequest, BaseUtil.getUserId(), false);
		List<LinkedHashMap<String,String>> headAllList = new ArrayList<>();
		List<List<Record>> resultAllList = new ArrayList<>();
		headAllList.add(this.initMainBdSalesQuantity());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");// 日期格式化
		resultAllList.add(page.getList().stream().map(distributorBdSalesQuantityReportResult -> {
			Record record = new Record();
			record.set("saleAreaName", distributorBdSalesQuantityReportResult.getSaleAreaName());
			record.set("productName", distributorBdSalesQuantityReportResult.getProductName());
			record.set("goodsSpec", distributorBdSalesQuantityReportResult.getGoodsSpec());
			record.set("customerName", distributorBdSalesQuantityReportResult.getCustomerName());
			record.set("gmtCreate", sdf.format(distributorBdSalesQuantityReportResult.getGmtCreate()));
			record.set("bdSalesQuantity", distributorBdSalesQuantityReportResult.getBdSalesQuantity());
			record.set("operationTypeName", distributorBdSalesQuantityReportResult.getOperationTypeName());
			record.set("bdName", distributorBdSalesQuantityReportResult.getBdName());
			return record;
		}).collect(Collectors.toList()));
		ExcelExportUtil.export(headAllList,resultAllList, CrmConstant.DISTRIBUTOR_BD_SALES_QUANTITY,getResponse(),null);
		renderNull();
	}

	/**
	 * 销售数量报表导出head
	 * @return
	 */
	public LinkedHashMap<String,String> initMainBdSalesQuantity() {
		LinkedHashMap<String,String> headList = new LinkedHashMap<>();
		for(CrmDistributorBdSalesQuantityReportEnum crmDistributorBdSalesQuantityReportEnum : CrmDistributorBdSalesQuantityReportEnum.values()) {
			headList.put(crmDistributorBdSalesQuantityReportEnum.getTypes(),crmDistributorBdSalesQuantityReportEnum.getName());
		}
		return headList;
	}
	
	/**
	 * 获取分销商销售区域
	 */
	public void querySaleAreaList(String name) {
		try {
			renderJson(R.ok().put("data",Arrays.stream(DistributorSaleAreaEnum.values()).map(distributorSaleAreaEnum -> {
				return new CommonCodeNameDTO(distributorSaleAreaEnum.getCode(), distributorSaleAreaEnum.getName());
			}).filter(distributorSaleAreaEnum -> StringUtils.isEmpty(name) ? true : distributorSaleAreaEnum.getName().contains(name)).collect(Collectors.toList())));
		} catch (Exception e) {
			logger.error(String.format("querySaleAreaList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
	}
	
	/**
	 * 获取销售大区
	 */
	public void queryBdDeptList(Long deptId) {
		try {
			
			renderJson(R.ok().put("data",adminDeptService.getSubDeptsByBusinessDepartmentId(Objects.nonNull(deptId) ? deptId : Long.valueOf(distributorBdDeptId))
					.stream().filter(record -> !distributorBdDeptIdExcutions.contains(Objects.nonNull(record.getStr("dept_id")) ? record.getStr("dept_id") : "")).collect(Collectors.toList())));
		} catch (NumberFormatException e) {
			logger.error(String.format("queryBdDeptList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
	}

	/**
	 * 获取分销商商品
	 */
	public void queryDistributorProductList(String productName) {
		try {
			renderJson(R.ok().put("data",crmDistributorReportService.queryDistributorProductList(productName,null,false)));
		} catch (NumberFormatException e) {
			logger.error(String.format("queryBdDeptList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
	}
	
	/**
	 * 获取分销商
	 */
	public void queryDistributorList(String customerName) {
		try {
			renderJson(R.ok().put("data",crmDistributorReportService.queryDistributorList(customerName,null,false)));
		} catch (NumberFormatException e) {
			logger.error(String.format("queryBdDeptList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DistributorSaleAreaReportTotalResult {
		
		Page<DistributorSaleAreaReportResult> page;
		
		// 订购数量（72crm_distributor_statistic.sales_quantity）
		private Long totalSalesQuantity;
		
		// 发货数量
		private Long totalDeliveryQuantity;

		// 销售数量(72crm_crm_customer_sales_log.sales_quantity)
		private Long totalBdSalesQuantity;
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DistributorBdReportTotalResult {
		
		Page<DistributorBdReportResult> page;
		
		// 订购数量（72crm_distributor_statistic.sales_quantity）
		private Long totalSalesQuantity;
		
		// 发货数量
		private Long totalDeliveryQuantity;

		// 销售数量(72crm_crm_customer_sales_log.sales_quantity)
		private Long totalBdSalesQuantity;
	}
}
