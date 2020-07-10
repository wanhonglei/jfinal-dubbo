package com.kakarote.crm9.erp.crm.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminBusinessStatusTypeService;
import com.kakarote.crm9.erp.crm.common.CrmPlanReportEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmPlanReport;
import com.kakarote.crm9.erp.crm.entity.CrmPlanReportResult;
import com.kakarote.crm9.erp.crm.service.CrmPlanReportService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.ExcelExportUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * CRM报表PPL
 *
 * @author yue.li
 */
public class CrmPlanReportController extends Controller {

    @Inject
    private AdminBusinessStatusTypeService adminBusinessStatusTypeService;

    @Inject
    private CrmPlanReportService crmPlanReportService;

    private Log logger = Log.getLog(getClass());

    /**
     * 查询PPL列表
     * @author yue.li
     * @param basePageRequest 分页对象
     *
     */
    @Permissions("board:plan:view")
    public void queryPlanReportList(BasePageRequest<CrmPlanReport> basePageRequest) {
        try{
            renderJson(R.ok().put("data", crmPlanReportService.queryPlanReportList(basePageRequest,BaseUtil.getUserId(),true)));
        }catch (Exception e){
            logger.error(String.format("queryPlanReportList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询阶段类别PPL未封信息
     * @author yue.li
     * return
     */
    public void queryBusinessStatusTypeForPlanReportList() {
        try{
            List<Record> recordList = adminBusinessStatusTypeService.queryBusinessStatusTypeForPlanReportList();
            renderJson(R.ok().put("data", crmPlanReportService.constructBusinessStatus(recordList)));
        }catch (Exception e){
            logger.error(String.format("queryBusinessStatusTypeForPlanReportList msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询回款期数信息
     * @author yue.li
     * return
     */
    public void queryReceivablesForPlanReportList(@Para("businessId")Integer businessId) {
        try{
            List<Record> recordList = crmPlanReportService.queryReceivablesForPlanReportList(businessId);
            renderJson(R.ok().put("data", recordList));
        }catch (Exception e){
            logger.error(String.format("queryReceivablesForPlanReportList msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * PPL报表导出
     * @author yue.li
     * return
     */
    @Permissions("board:plan:export")
    public void exportPlanReportExcel(BasePageRequest<CrmPlanReport> basePageRequest) throws Exception{
        CrmPlanReportResult crmPlanReportResult= crmPlanReportService.queryPlanReportList(basePageRequest,BaseUtil.getUserId(),false);
        List<LinkedHashMap<String,String>> headAllList = new ArrayList<>();
        List<List<Record>> resultAllList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Record record:crmPlanReportResult.getPageResult().getList()){
            record.set("productName",record.getStr("productName").replaceAll(",","\r\n"));
            record.set("dealDate", StringUtils.isNotEmpty(record.getStr("dealDate")) ? sdf.format(sdf.parse(record.getStr("dealDate"))) : null);
        }
        headAllList.add(initMain());
        resultAllList.add(crmPlanReportResult.getPageResult().getList());
        ExcelExportUtil.export(headAllList,resultAllList, CrmConstant.PLAN_REPORT,getResponse(),null);
        renderNull();
    }

    /***
     * PPL导出head
     * @author yue.li
     */
    private  LinkedHashMap<String,String> initMain() {
        LinkedHashMap<String,String> headList = new LinkedHashMap<>();
        for(CrmPlanReportEnum crmPlanReportEnum : CrmPlanReportEnum.values()) {
            headList.put(crmPlanReportEnum.getTypes(),crmPlanReportEnum.getName());
        }
        return headList;
    }

    /**
     * 根据事业部ID获取商机组信息(包含事业部ID)
     * @author yue.li
     * @param deptId 事业部ID
     * return
     */
    public void getBusinessGroupByDeptId(@Para("deptId")Long deptId) {
        try{
            renderJson(R.ok().put("data", crmPlanReportService.getBusinessGroupContainsDeptByDeptId(deptId)));
        }catch (Exception e){
            logger.error(String.format("getBusinessGroupByDeptId msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据商机组获取商机阶段
     * @author yue.li
     * @param groupId 商机组ID
     * return
     */
    public void getBusinessStatusListByGroupId(@Para("groupId")Integer groupId) {
        try{
            renderJson(R.ok().put("data", crmPlanReportService.getBusinessStatusListByGroupId(groupId)));
        }catch (Exception e){
            logger.error(String.format("getBusinessStatusListByGroupId msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 报表获取事业部
     * @author yue.li
     * return
     */
    public void getDeptList() {
        try{
            renderJson(R.ok().put("data", crmPlanReportService.getDeptList()));
        }catch (Exception e){
            logger.error(String.format("getDeptList msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

}