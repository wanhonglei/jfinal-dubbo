package com.kakarote.crm9.erp.crm.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.common.CrmDepartmentIncomeExcelEnum;
import com.kakarote.crm9.erp.crm.common.CrmSaleUsualEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmDepartmentIncomeReportDto;
import com.kakarote.crm9.erp.crm.entity.CrmDepartmentIncomeResult;
import com.kakarote.crm9.erp.crm.service.CrmDepartmentIncomeService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.CrmReportDeptUtil;
import com.kakarote.crm9.utils.ExcelExportUtil;
import com.kakarote.crm9.utils.R;

/**
 * CRM部门收入报表
 *
 * @author yue.li
 */
public class CrmDepartmentIncomeReportController extends Controller {

    @Inject
    private CrmDepartmentIncomeService crmDepartmentIncomeService;
//    @Inject
//    private CrmBusinessService crmBusinessService;

    private Log logger = Log.getLog(getClass());

    /**
     * 查询部门收入列表
     * @author yue.li
     * @param basePageRequest 分页对象
     */
    @Permissions("board:departmentIncome:view")
    public void queryDepartmentIncomeReportList(BasePageRequest<CrmDepartmentIncomeReportDto> basePageRequest) {
        try{
            renderJson(R.ok().put("data", crmDepartmentIncomeService.queryDepartmentIncomeReportList(basePageRequest,BaseUtil.getUserId())));
        }catch (Exception e){
            logger.error(String.format("queryDepartmentIncomeReportList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取部门收入对应的部门
     * @author yue.li
     * return
     */
    public void getDeptIncomeReportDeptList() {
        try{
            renderJson(R.ok().put("data",CrmReportDeptUtil.getCrmReportDeptList()));
        }catch (Exception e){
            logger.error(String.format("getDeptIncomeReportDeptList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询部门收入列表导出
     * @author yue.li
     * @param basePageRequest 分页对象
     */
    @Permissions("board:departmentIncome:export")
    public void exportDeptIncomeReportExcel(BasePageRequest<CrmDepartmentIncomeReportDto> basePageRequest) throws Exception{
        CrmDepartmentIncomeResult crmDepartmentIncomeResult = crmDepartmentIncomeService.queryDepartmentIncomeReportList(basePageRequest,BaseUtil.getUserId());
        crmDepartmentIncomeResult.setCurrentIncome(addCurrentIncomeVisitOrRelation(crmDepartmentIncomeResult));
        List<LinkedHashMap<String,String>> headAllList = new ArrayList<>();
        List<List<Record>> resultAllList = new ArrayList<>();
        Map<String, BigDecimal> currentIncome = crmDepartmentIncomeResult.getCurrentIncome();
        Map<String, Map<String, BigDecimal>> provinceProductIncome = crmDepartmentIncomeResult.getProvinceProductIncome();
        List<Record> dataList = new ArrayList<>();
        List<Record> dataDetailList = new ArrayList<>();
        LinkedHashMap<String,String> headMainMap = new LinkedHashMap<>();
        LinkedHashMap<String,String> headDetailMap = new LinkedHashMap<>();
        List<String> mergeList = new ArrayList<>();
        /**商品总统计*/
        if(currentIncome != null){
            Record record = new Record();
            for(Map.Entry<String, BigDecimal> currentIncomeMap : currentIncome.entrySet()) {
                headMainMap.put(currentIncomeMap.getKey(),currentIncomeMap.getKey());
                record.set(currentIncomeMap.getKey(),currentIncomeMap.getValue() == null ? null : currentIncomeMap.getValue().toString());
            }
            headAllList.add(headMainMap);
            dataList.add(record);
            resultAllList.add(dataList);
            mergeList.add(CrmDepartmentIncomeExcelEnum.PRODUCT_MAIN_INCOME_DESC_KEY.getName());
        }
        /**按省统计商品*/
        if(provinceProductIncome != null) {
            for(Map.Entry<String, Map<String,BigDecimal>> provinceProductIncomeMap : provinceProductIncome.entrySet()) {
                String province = provinceProductIncomeMap.getKey();
                Map<String,BigDecimal> provinceProductDetailIncome = provinceProductIncomeMap.getValue();
                Record record = new Record();
                record.set(CrmConstant.PROVINCE,province);
                for(Map.Entry<String, String> headMainMapSet : headMainMap.entrySet()) {
                    if(provinceProductDetailIncome.get(headMainMapSet.getKey()) != null) {
                        if(!CrmSaleUsualEnum.VISIT_COUNT_TYPE_KEY.getName().equals(headMainMapSet.getKey()) && !CrmSaleUsualEnum.RELATION_COUNT_TYPE_KEY.getName().equals(headMainMapSet.getKey())){
                            record.set(headMainMapSet.getKey(),provinceProductDetailIncome.get(headMainMapSet.getKey()).setScale(2,BigDecimal.ROUND_HALF_UP));
                        }else{
                            record.set(headMainMapSet.getKey(),provinceProductDetailIncome.get(headMainMapSet.getKey()));
                        }

                    } else {
                        record.set(headMainMapSet.getKey(),"0.00");
                    }
                }
                dataDetailList.add(record);
            }

            headDetailMap.putAll(headMainMap);
            headDetailMap.put(CrmConstant.PROVINCE,CrmConstant.PROVINCE);
            headAllList.add(headDetailMap);
            resultAllList.add(dataDetailList);
            mergeList.add(CrmDepartmentIncomeExcelEnum.PRODUCT_DETAIL_INCOME_DESC_KEY.getName());
        }

        ExcelExportUtil.export(headAllList,resultAllList, CrmConstant.DEPT_INCOME_REPORT,getResponse(),mergeList);
        renderNull();
    }

    /**
     * 商品总统计中添加拜访联系数总统计
     * @author yue.li
     * @param crmDepartmentIncomeResult 统计结果集
     * @return
     */
    public Map<String,BigDecimal> addCurrentIncomeVisitOrRelation (CrmDepartmentIncomeResult crmDepartmentIncomeResult) {
        BigDecimal visitTotal = new BigDecimal(0);
        BigDecimal relationTotal = new BigDecimal(0);
        Map<String, Map<String, BigDecimal>> provinceProductIncome = crmDepartmentIncomeResult.getProvinceProductIncome();
        if(provinceProductIncome != null) {
            for (Map.Entry<String, Map<String,BigDecimal>> productProductIncome : provinceProductIncome.entrySet()) {
                for(Map.Entry<String, BigDecimal> productIncome : productProductIncome.getValue().entrySet()) {
                    if(productIncome.getKey().equals(CrmSaleUsualEnum.VISIT_COUNT_TYPE_KEY.getName())){
                        visitTotal = visitTotal.add(productIncome.getValue());
                    }
                    if(productIncome.getKey().equals(CrmSaleUsualEnum.RELATION_COUNT_TYPE_KEY.getName())){
                        relationTotal = relationTotal.add(productIncome.getValue());
                    }
                }
            }
        }
        Map<String,BigDecimal> currentIncome = crmDepartmentIncomeResult.getCurrentIncome();
        currentIncome.put(CrmSaleUsualEnum.VISIT_COUNT_TYPE_KEY.getName(),visitTotal);
        currentIncome.put(CrmSaleUsualEnum.RELATION_COUNT_TYPE_KEY.getName(),relationTotal);
        return currentIncome;
    }
}