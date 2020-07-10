package com.kakarote.crm9.erp.crm.controller;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.common.CrmSaleUsualDetailExcelEnum;
import com.kakarote.crm9.erp.crm.common.CrmSaleUsualExcelEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmSaleUsualReportDto;
import com.kakarote.crm9.erp.crm.service.CrmSaleUsualService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.ExcelExportUtil;
import com.kakarote.crm9.utils.R;

/**
 * CRM销售日常跟进报表
 *
 * @author yue.li
 */
public class CrmSaleUsualReportController extends Controller {

    @Inject
    private CrmSaleUsualService crmSaleUsualService;
//    @Inject
//    private CrmBusinessService crmBusinessService;

    private Log logger = Log.getLog(getClass());

    /**
     * 查询销售日常跟进列表
     * @author yue.li
     * @param basePageRequest 分页对象
     */
    @Permissions("board:saleUsual:view")
    public void querySaleUsualReportList(BasePageRequest<CrmSaleUsualReportDto> basePageRequest) {
        try{
            renderJson(R.ok().put("data", crmSaleUsualService.querySaleUsualReportList(basePageRequest,BaseUtil.getUserId(),true)));
        }catch (Exception e){
            logger.error(String.format("querySaleUsualReportList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询销售日常跟进导出
     * @author yue.li
     * @param basePageRequest 分页对象
     */
    @Permissions("board:saleUsual:export")
    public void exportSaleUsualReportExcel(BasePageRequest<CrmSaleUsualReportDto> basePageRequest) throws Exception{
        Page<Record> resultList = crmSaleUsualService.querySaleUsualReportList(basePageRequest,BaseUtil.getUserId(),false);
        List<LinkedHashMap<String,String>> headAllList = new ArrayList<>();
        List<List<Record>> resultAllList = new ArrayList<>();
        List<Record> dataList = resultList.getList();

        headAllList.add(initMain());

        resultAllList.add(dataList);

        ExcelExportUtil.export(headAllList,resultAllList, CrmConstant.SALE_USUAL_REPORT,getResponse(),null);
        renderNull();
    }

    /***
     * 明细封装导出head
     * @author yue.li
     */
    public  LinkedHashMap<String,String> initDetail() {
        LinkedHashMap<String,String> headList = new LinkedHashMap<>();
        headList.put(CrmSaleUsualDetailExcelEnum.SALES_USER_KEY.getTypes(),CrmSaleUsualDetailExcelEnum.SALES_USER_KEY.getName());
        headList.put(CrmSaleUsualDetailExcelEnum.PRODUCT_CATEGORY_KEY.getTypes(),CrmSaleUsualDetailExcelEnum.PRODUCT_CATEGORY_KEY.getName());
        headList.put(CrmSaleUsualDetailExcelEnum.PRODUCT_KEY.getTypes(),CrmSaleUsualDetailExcelEnum.PRODUCT_KEY.getName());
        headList.put(CrmSaleUsualDetailExcelEnum.INCOME_KEY.getTypes(),CrmSaleUsualDetailExcelEnum.INCOME_KEY.getName());
        return headList;
    }

    /***
     * 销售日常报表导出head
     * @author yue.li
     */
    public  LinkedHashMap<String,String> initMain() {
        LinkedHashMap<String,String> headList = new LinkedHashMap<>();
        for(CrmSaleUsualExcelEnum crmSaleUsualExcelEnum : CrmSaleUsualExcelEnum.values()) {
            headList.put(crmSaleUsualExcelEnum.getTypes(),crmSaleUsualExcelEnum.getName());
        }
        return headList;
    }

    /**
     * 获取销售日常报表对应的部门
     * @author yue.li
     * return
     */
    public void getSaleUsualDeptList() {
        try{
            renderJson(crmSaleUsualService.getSaleUsualDeptList());
        }catch (Exception e){
            logger.error(String.format("getSaleUsualDeptList adminScenario msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
}