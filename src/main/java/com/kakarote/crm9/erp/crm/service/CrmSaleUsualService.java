package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmRegionEnum;
import com.kakarote.crm9.erp.crm.common.CrmSaleUsualEnum;
import com.kakarote.crm9.erp.crm.common.CrmSaleUsualExcelEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmSaleUsualReportDto;
import com.kakarote.crm9.erp.crm.entity.CustomerRevenueStatistic;
import com.kakarote.crm9.utils.CrmReportDeptUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class CrmSaleUsualService {

//    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private CrmBusinessService crmBusinessService;

    @Inject
    private AdminDataDicService adminDataDicService;

    /****
     * 获取销售日常跟进报表
     */
    public Page<Record> querySaleUsualReportList(BasePageRequest<CrmSaleUsualReportDto> request, Long userId, boolean isPage) {
        Integer deptId = request.getData().getDeptId();
        String saleUserId = request.getData().getSaleUserId();
        String startTime = request.getData().getStartTime();
        String endTime =  request.getData().getEndTime();
        List<Integer> excludeDeptIds = new ArrayList<>();
        Page<Record> paginate = new Page<>();
        /**查询数据权限*/
        List<Long> longs= Aop.get(AdminUserService.class).queryUserByAuth(userId);
        List<Integer> deptIds = adminDeptService.getDeptAndBranchDept(deptId);

        /**过滤部门集合*/
        List<Integer> excludeSaleUsualList = excludeSaleUsualDepartment();
        if(excludeSaleUsualList != null && excludeSaleUsualList.size() >0) {
            for(Integer excludeDept:excludeSaleUsualList){
                excludeDeptIds.addAll(adminDeptService.getDeptAndBranchDept(excludeDept));
            }
        }

        SqlPara sqlPara = Db.getSqlPara("crm.saleUsual.querySaleUsualReportList",
                Kv.by("saleUserId", saleUserId).set("deptIds", deptIds).set("owner_user_id",longs).set("excludeDeptIds",excludeDeptIds));
        if(isPage){
            paginate = Db.paginate(request.getPage(), request.getLimit(),sqlPara);
        }else{
            paginate.setList(Db.find(sqlPara));
        }
        if(paginate.getList() != null && paginate.getList().size() >0){
            /**构造所需要的数据*/
            List<Record> recordList = paginate.getList();
            for(Record record : recordList){
                int visitCount = sumVisitOrRelationCount(startTime,endTime,null,CrmSaleUsualEnum.VISIT_TYPE_KEY.getTypes(),record.getStr("saleUserId"));
                int visitLeadsCount = sumVisitOrRelationLeadsCount(startTime,endTime,null,CrmSaleUsualEnum.VISIT_TYPE_KEY.getTypes(),record.getStr("saleUserId"));
                int relationCount = sumVisitOrRelationCount(startTime,endTime,null,CrmSaleUsualEnum.RELATION_TYPE_KEY.getTypes(),record.getStr("saleUserId"));
                int relationLeadsCount = sumVisitOrRelationLeadsCount(startTime,endTime,null,CrmSaleUsualEnum.RELATION_TYPE_KEY.getTypes(),record.getStr("saleUserId"));
                int regionCustomerCount = sumVisitOrRelationRegionCount(startTime,endTime,null,null,record.getStr("saleUserId"));
                int totalWinBillsCount = sumWinBillCount(startTime,endTime,record.getStr("saleUserId"));
                int visitWinBillsCount = sumWinVisitBillCount(startTime,endTime,record.getStr("saleUserId"),CrmSaleUsualEnum.VISIT_TYPE_KEY.getTypes());
                CustomerRevenueStatistic customerRevenueStatistic = crmBusinessService.calculateCustomerRevenue(crmBusinessService.getBusinessRevenueStatisticInfo(getBusinessIdsByOwnUserId(record.getStr("saleUserId")),startTime,endTime));
                BigDecimal relevanceTotalIncome = customerRevenueStatistic == null ? new BigDecimal("0.00"): customerRevenueStatistic.getTotalRevenue();
                BigDecimal noRelevanceTotalIncome = crmBusinessService.queryUserPayment(startTime,endTime,record.getStr("saleUserId"),null,CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER,null);
                record.set(CrmSaleUsualExcelEnum.VISIT_COUNT_KEY.getTypes(),visitCount + visitLeadsCount);
                record.set(CrmSaleUsualExcelEnum.RELATION_COUNT_KEY.getTypes(),relationCount + relationLeadsCount);
                record.set(CrmSaleUsualExcelEnum.TOTAL_VISIT_RELATION_COUNT_KEY.getTypes(),visitCount + visitLeadsCount + relationCount + relationLeadsCount);
                record.set(CrmSaleUsualExcelEnum.REGION_CUSTOMER_COUNT_KEY.getTypes(),regionCustomerCount);
                record.set(CrmSaleUsualExcelEnum.REGION_CUSTOMER_RATIO.getTypes(),getRatio(regionCustomerCount,visitCount + visitLeadsCount + relationCount + relationLeadsCount));
                record.set(CrmSaleUsualExcelEnum.TOTAL_WIN_BILLS_COUNT.getTypes(),totalWinBillsCount);
                record.set(CrmSaleUsualExcelEnum.VISIT_WIN_BILLS_COUNT.getTypes(),visitWinBillsCount);
                record.set(CrmSaleUsualExcelEnum.TOTAL_WIN_BILLS_RATIO.getTypes(),getRatio(totalWinBillsCount,visitCount + visitLeadsCount + relationCount + relationLeadsCount));
                record.set(CrmSaleUsualExcelEnum.VISIT_WIN_BILLS_RATIO.getTypes(),getRatio(visitWinBillsCount,visitCount + visitLeadsCount));
                record.set(CrmSaleUsualExcelEnum.RELEVANCE_TOTAL_INCOME.getTypes(),relevanceTotalIncome);
                record.set(CrmSaleUsualExcelEnum.NO_RELEVANCE_TOTAL_INCOME.getTypes(),noRelevanceTotalIncome);
                record.set(CrmSaleUsualExcelEnum.TOTAL_INCOME.getTypes(),relevanceTotalIncome.add(noRelevanceTotalIncome));
                record.set(CrmConstant.PRODUCT_REVENUE_MAP,customerRevenueStatistic == null? null :customerRevenueStatistic.getCategoryCodeProductRevenueMap());
            }
            /**构造所需要的数据*/
        }
        return paginate;
    }

    /***
     * 统计拜访、联系数量(客户、商机、联系人)
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @param category  0:拜访 1:联系
     * @param createUserId 创建人
     * @return
     */
    public int sumVisitOrRelationCount(String startTime, String endTime, String ownUserId,String category,String createUserId){
        int count = 0;
        List<Record> list = VisitOrRelation(startTime,endTime,ownUserId,category,null,createUserId);
        if(list != null && list.size() >0) {
            count = list.size();
        }
        return count;
    }

    /***
     * 统计拜访、联系数量(地市级别)
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @param category  0:拜访 1:联系
     * @param createUserId 创建人
     * @return
     */
    public int sumVisitOrRelationRegionCount(String startTime, String endTime, String ownUserId,String category,String createUserId ){
        int count = 0;
        List<Record> regionList = new ArrayList<>();
        List<Record> list = new ArrayList<>();
        List<Record> visitList = VisitOrRelation(startTime,endTime,ownUserId,CrmSaleUsualEnum.VISIT_TYPE_KEY.getTypes(),CrmSaleUsualEnum.IS_ADDRESS_TYPE_KEY.getTypes(),createUserId);
        List<Record> relationList = VisitOrRelation(startTime,endTime,ownUserId,CrmSaleUsualEnum.RELATION_TYPE_KEY.getTypes(),CrmSaleUsualEnum.IS_ADDRESS_TYPE_KEY.getTypes(),createUserId);
        list.addAll(visitList);
        list.addAll(relationList);
        if(list != null && list.size() >0){
            for(int i = 0; i < list.size(); i++){
                for(CrmRegionEnum regionEnum: CrmRegionEnum.values()) {
                    if(list.get(i).getStr("address").contains(regionEnum.getName())) {
                        regionList.add(list.get(i));
                        break;
                    }
                }
            }
            if(regionList != null && regionList.size() >0) {
                count = list.size() - regionList.size();
            }else{
                count = list.size();
            }
        }
        return count;
    }

    /***
     * 统计拜访、联系数量(以负责人对应的客户作为基准)
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @param category  0:拜访 1:联系
     * @param isAddress 2:只查询有省市区地址的客户信息
     * @param createUserId 创建人
     * @return
     */
    public List<Record> VisitOrRelation(String startTime, String endTime, String ownUserId,String category,String isAddress,String createUserId){
        List<Record>  list = Db.find(Db.getSqlPara("crm.saleUsual.queryCustomerRecord", Kv.by("startTime", startTime).set("endTime", endTime).set("ownUserId",ownUserId).set("category",category).set("isAddress",isAddress).set("createUserId",createUserId)));
        return list;
    }

    /***
     * 获取百分比
     * @author yue.li
     * @param molecule 分子
     * @param denominator 分母
     * @return
     */
    public String getRatio(int molecule,int denominator){
        if(denominator == 0) {
            return denominator + CrmConstant.PERCENT;
        }else{
            return new BigDecimal(molecule).divide(new BigDecimal(denominator),2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale( 0, BigDecimal.ROUND_UP ) + CrmConstant.PERCENT;
        }
    }

    /***
     * 统计总成单数
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @return
     */
    public int sumWinBillCount(String startTime, String endTime, String ownUserId){
        int count = 0;
        List<Record> list = winBill(startTime,endTime,ownUserId);
        List<Record> noContactList = winBillNoContactReceivables(startTime,endTime,ownUserId);
        if(CollectionUtils.isNotEmpty(list)) {
            count = list.size();
        }
        if(CollectionUtils.isNotEmpty(noContactList)){
            count = count + noContactList.size();
        }
        return count;
    }

    /***
     * 统计拜访成单数
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @param category  0:拜访 1:联系
     * @return
     */
    public int sumWinVisitBillCount(String startTime, String endTime, String ownUserId,String category){
        int count = 0;
        List<Record> list = winVisitBill(startTime,endTime,ownUserId,category);
        List<Record> noContactList = winVisitBillNoContactReceivables(startTime,endTime,ownUserId,category);
        if(list != null && list.size() >0) {
            count = list.size();
        }
        if(CollectionUtils.isNotEmpty(noContactList)){
            count = count + noContactList.size();
        }
        return count;
    }

    /***
     * 总成单数
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @return
     */
    public List<Record> winBill(String startTime, String endTime, String ownUserId){
        List<Record>  list = Db.find(Db.getSqlPara("crm.saleUsual.queryWinBillCustomerRecord", Kv.by("startTime", startTime).set("endTime", endTime).set("ownUserId",ownUserId)));
        return list;
    }

    /***
     * 总成单数(未关联回款)
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @return
     */
    public List<Record> winBillNoContactReceivables(String startTime, String endTime, String ownUserId){
        return Db.find(Db.getSqlPara("crm.saleUsual.queryWinBillContactCustomerRecord", Kv.by("startTime", startTime).set("endTime", endTime).set("ownUserId",ownUserId).set("status",CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER)));
    }

    /***
     * 拜访成单数
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @param category  0:拜访 1:联系
     * @return
     */
    public List<Record> winVisitBill(String startTime, String endTime, String ownUserId,String category){
        List<Record>  list = Db.find(Db.getSqlPara("crm.saleUsual.queryWinBillVisitCustomerRecord", Kv.by("startTime", startTime).set("endTime", endTime).set("ownUserId",ownUserId).set("category",category)));
        return list;
    }

    /***
     * 拜访成单数(未关联回款)
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @param category  0:拜访 1:联系
     * @return
     */
    public List<Record> winVisitBillNoContactReceivables(String startTime, String endTime, String ownUserId,String category){
        return Db.find(Db.getSqlPara("crm.saleUsual.queryWinBillVisitNoContactCustomerRecord", Kv.by("startTime", startTime).set("endTime", endTime).set("ownUserId",ownUserId).set("category",category).set("status",CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER)));
    }

    /***
     * 获取BD下的所有商机IDS
     * @author yue.li
     * @param ownUserId 负责人
     * @return
     */
    public List<Integer> getBusinessIdsByOwnUserId(String ownUserId){
        if(StringUtils.isNotEmpty(ownUserId)){
            List<Record>  list = Db.find(Db.getSqlPara("crm.saleUsual.queryBusinessIdsByUserId", Kv.by("ownUserId", ownUserId)));
            return list.stream().map(record -> record.getInt("business_id")).collect(Collectors.toList());
        }else{
            return null;
        }
    }

    /***
     * 获取商品大类收入
     * @author yue.li
     * @param resultMap 商品大类收入结果集
     * @param category 商品大类
     */
    public BigDecimal getCategoryIncome(Map<String, BigDecimal> resultMap,String category) {
        BigDecimal resultIncome = new BigDecimal("0.00");
        if(resultMap != null) {
            for (Map.Entry<String, BigDecimal> entry : resultMap.entrySet()) {
                if(entry.getKey().equals(category)) {
                    resultIncome =  entry.getValue();
                    break;
                }
            }
        }
        return resultIncome;
    }

    /**
     * 获取销售日常报表展示部门列表
     * @author yue.li
     */
    public R getSaleUsualDeptList() {
        List<Record> resultList = CrmReportDeptUtil.getCrmReportDeptList();
        List<Record> removeDeptList = new ArrayList<>();
        List<Integer> excludeSaleUsualDeptList = excludeSaleUsualDepartment();

        /**过滤报表展示部门*/
        if(resultList != null && resultList.size() >0 && excludeSaleUsualDeptList != null && excludeSaleUsualDeptList.size() >0 ) {
            for(Record businessDept : resultList) {
                for(Integer excludeSaleUsualDept: excludeSaleUsualDeptList) {
                    if(excludeSaleUsualDept != null && businessDept.getStr("id").equals(String.valueOf(excludeSaleUsualDept))) {
                        removeDeptList.add(businessDept);
                    }
                }
            }
            resultList.removeAll(removeDeptList);
        }
        return R.ok().put("data", resultList);
    }

    /***
     * 统计拜访、联系数量(线索)
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @param category  0:拜访 1:联系
     * @param createUserId 创建人
     * @return
     */
    public int sumVisitOrRelationLeadsCount(String startTime, String endTime, String ownUserId,String category,String createUserId){
        int count = 0;
        List<Record> list = VisitOrRelationLeads(startTime,endTime,ownUserId,category,createUserId);
        if(list != null && list.size() >0) {
            count = list.size();
        }
        return count;
    }

    /***
     * 统计拜访、联系数量(线索)
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人
     * @param category  0:拜访 1:联系
     * @param createUserId 创建人
     * @return
     */
    public List<Record> VisitOrRelationLeads(String startTime, String endTime, String ownUserId,String category,String createUserId){
        List<Record>  list = Db.find(Db.getSqlPara("crm.saleUsual.queryLeadsRecord", Kv.by("startTime", startTime).set("endTime", endTime).set("ownUserId",ownUserId).set("category",category).set("createUserId",createUserId)));
        return list;
    }

    /***
     * 过滤销售日常报表部门集合
     * @author yue.li
     * @return
     */
    public List<Integer> excludeSaleUsualDepartment(){
        List<Integer> resultList = new ArrayList<>();
        List<Record> saleUsualExcludeList = adminDataDicService.queryDataDicList(CrmConstant.SALE_USUAL_REPORT_TYPE);
        if(saleUsualExcludeList != null && saleUsualExcludeList.size() >0){
            for(Record record : saleUsualExcludeList){
                Integer deptId = adminDeptService.getDeptIdByDeptName(record.get("name"));
                if(deptId != null){
                    resultList.add(deptId);
                }
            }
        }
        return resultList;
    }
}