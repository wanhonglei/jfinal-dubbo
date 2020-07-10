package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmWinRateEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmDepartmentIncomeReportDto;
import com.kakarote.crm9.erp.crm.entity.BusinessRevenueInfo;
import com.kakarote.crm9.erp.crm.entity.CrmDepartmentIncomeResult;
import com.kakarote.crm9.erp.crm.entity.CustomerRevenueStatistic;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.CrmDateUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CrmDepartmentIncomeService {

    private Log logger = Log.getLog(getClass());

    @Inject
    private CrmBusinessService crmBusinessService;

    @Inject
    private AdminUserService adminUserService;

    /****
     * 获取部门收入报表
     * @author yue.li
     * @param request 请求实体
     * @param userId 用户ID
     * @return
     */
    public CrmDepartmentIncomeResult queryDepartmentIncomeReportList(BasePageRequest<CrmDepartmentIncomeReportDto> request, Long userId) {
        Integer deptId = request.getData().getDeptId();
        String startTime = request.getData().getStartTime();
        String endTime =  request.getData().getEndTime();
        CrmDepartmentIncomeResult crmPlanReportResult = new CrmDepartmentIncomeResult();
        List<Long> deptOwnUserIds = new ArrayList<>();
        /**查询部门负责人*/
        if(deptId != null) {
            deptOwnUserIds = adminUserService.getUserIdsByDeptIds(adminUserService.queryMyDeptAndSubDeptId(deptId));
        }
        /**查询数据权限*/
        List<Long> longs= Aop.get(AdminUserService.class).queryUserByAuth(userId);

        List<Record> recordList = Db.find(Db.getSqlPara("crm.departmentIncome.queryBusinessByDeptList",
                Kv.by("deptId", deptId).set("owner_user_id",longs)));

        List<Integer> businessList = recordList.stream().map(record -> record.getInt("business_id")).collect(Collectors.toList());
        Map<Long, BusinessRevenueInfo> businessRevenueStatisticInfo = getBusinessRevenueStatisticInfo(businessList,startTime,endTime);
        Map<String,Map<Long,BigDecimal>> provinceProductIncome = crmBusinessService.provinceProductIncome(businessRevenueStatisticInfo,recordList);
        crmPlanReportResult.setCurrentIncome(currentIncome(businessRevenueStatisticInfo,startTime,endTime,deptOwnUserIds,longs));
        crmPlanReportResult.setMonthForecast(winRateForecast(deptId,longs,startTime));
        crmPlanReportResult.setProvinceProductIncome(crmBusinessService.productIdConvertToProductName(businessRevenueStatisticInfo,provinceProductIncome,startTime,endTime,deptOwnUserIds,longs));
        return crmPlanReportResult;
    }

    /***
     * 统计当期商品收入
     * @author yue.li
     * @param businessRevenueStatisticInfo 商机统计商品集合
     * @return
     */
    public Map<String, BigDecimal> currentIncome(Map<Long, BusinessRevenueInfo> businessRevenueStatisticInfo,String startTime,String endTime,List<Long> deptOwnUserIds,List<Long> longs) {
        Map<String,BigDecimal> map = new LinkedHashMap<>();
        BigDecimal currentIncomeTotal = new BigDecimal(0);
        CustomerRevenueStatistic customerRevenueStatistic = crmBusinessService.calculateCustomerRevenue(businessRevenueStatisticInfo);
        BigDecimal relevanceTotalIncome = crmBusinessService.queryUserPayment(startTime,endTime,null,deptOwnUserIds,CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER,longs);
        if(customerRevenueStatistic != null) {
            Map<String, Map<String, BigDecimal>> categoryCodeProductRevenue = customerRevenueStatistic.getCategoryCodeProductRevenueMap();
            if(categoryCodeProductRevenue != null) {
                /**统计当期商品收入*/
                for(Map.Entry<String, Map<String,BigDecimal>> entry : categoryCodeProductRevenue.entrySet()) {
                    for (Map.Entry<String, BigDecimal> productIncome : categoryCodeProductRevenue.get(entry.getKey()).entrySet()) {
                        if(productIncome.getKey() != null) {
                            map.put(productIncome.getKey(),productIncome.getValue());
                        }
                    }
                }
                /**统计当期商品总收入*/
                if(map != null) {
                    for(Map.Entry<String, BigDecimal> resultMap :map.entrySet()) {
                        currentIncomeTotal = currentIncomeTotal.add(resultMap.getValue());
                    }
                }
            }
        }
        map.put(CrmConstant.RELEVANCE_TOTAL_INCOME,currentIncomeTotal.setScale( 2, BigDecimal.ROUND_UP ));
        map.put(CrmConstant.NO_RELEVANCE_TOTAL_INCOME,relevanceTotalIncome.setScale( 2, BigDecimal.ROUND_UP ));
        map.put(CrmConstant.TOTAL_INCOME,currentIncomeTotal.add(relevanceTotalIncome).setScale( 2, BigDecimal.ROUND_UP ));
        return map;
    }

    /***
     * 统计赢率当月预测信息
     * @author yue.li
     * @param deptId 部门
     * @param userIds 人员集合
     * @param startTime 开始时间
     * @return
     */
    public Map<String,BigDecimal> winRateForecast(Integer deptId,List<Long> userIds,String startTime) {
        Map<String,BigDecimal> winRateMap = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        BigDecimal wonTotal = BigDecimal.ZERO;
        BigDecimal worstTotal = BigDecimal.ZERO;
        BigDecimal forecastTotal = BigDecimal.ZERO;
        BigDecimal riskTotal = BigDecimal.ZERO;
        try{
            if(StringUtils.isNotEmpty(startTime)) {
                String searchStartTime = sdf.format(CrmDateUtil.getFirstDayDateOfMonth(sdf.parse(startTime)));
                String searchEndTime = sdf.format(CrmDateUtil.getLastDayOfMonth(sdf.parse(startTime)));
                List<Record> recordList = Db.find(Db.getSqlPara("crm.departmentIncome.queryDepartmentIncomeWinRateList",
                        Kv.by("deptId", deptId).set("owner_user_id",userIds).set("startTime",searchStartTime).set("endTime",searchEndTime)));
                if(recordList != null && recordList.size() >0) {
                    for(Record record : recordList) {
                        if(record.get("winRate").equals(CrmWinRateEnum.WON_TYPE_KEY.getTypes())){
                            wonTotal = new BigDecimal(record.getStr("money"));
                        }
                        if(record.get("winRate").equals(CrmWinRateEnum.WORST_TYPE_KEY.getTypes())){
                            worstTotal = new BigDecimal(record.getStr("money"));
                        }
                        if(record.get("winRate").equals(CrmWinRateEnum.FORECAST_TYPE_KEY.getTypes())){
                            forecastTotal = new BigDecimal(record.getStr("money"));
                        }
                        if(record.get("winRate").equals(CrmWinRateEnum.RISK_TYPE_KEY.getTypes())){
                            riskTotal = new BigDecimal(record.getStr("money"));
                        }
                    }
                }
            }
            winRateMap.put(CrmWinRateEnum.WON_TYPE_KEY.getTypes(),wonTotal.setScale( 2, BigDecimal.ROUND_UP ));
            winRateMap.put(CrmWinRateEnum.WORST_TYPE_KEY.getTypes(),worstTotal.setScale( 2, BigDecimal.ROUND_UP ));
            winRateMap.put(CrmWinRateEnum.FORECAST_TYPE_KEY.getTypes(),forecastTotal.setScale( 2, BigDecimal.ROUND_UP ));
            winRateMap.put(CrmWinRateEnum.RISK_TYPE_KEY.getTypes(),riskTotal.setScale( 2, BigDecimal.ROUND_UP ));
            winRateMap.put(CrmWinRateEnum.BEST_TYPE_KEY.getTypes(),wonTotal.add(worstTotal).add(forecastTotal).add(riskTotal).setScale( 2, BigDecimal.ROUND_UP ));
        }catch(Exception e) {
            logger.error(String.format("winRateForecast msg:%s", BaseUtil.getExceptionStack(e)));
        }
        return winRateMap;
    }

    /***
     * 根据商机集合和日期期间获取商品收入统计
     * @author yue.li
     * @param businessList 商机集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    public Map<Long, BusinessRevenueInfo> getBusinessRevenueStatisticInfo(List<Integer> businessList,String startTime,String endTime) {
        return crmBusinessService.getBusinessRevenueStatisticInfo(businessList,startTime,endTime);
    }

}