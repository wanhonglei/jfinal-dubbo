package com.kakarote.crm9.erp.crm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.CrmEventAnnotation;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.common.CrmEventEnum;
import com.kakarote.crm9.erp.crm.common.CrmSignInExcelEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmSignInPageRequest;
import com.kakarote.crm9.erp.crm.service.CrmNotesService;
import com.kakarote.crm9.erp.crm.service.CrmSignInService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.CrmDateUtil;
import com.kakarote.crm9.utils.ExcelExportUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Crm Sign In Controller
 *
 * @author hao.fu
 * @since 2019/11/19 15:21
 */
@Before(IocInterceptor.class)
public class CrmSignInController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private CrmSignInService crmSignInService;

    @Inject
    private CrmNotesService crmNotesService;

    /**
     * Get sign in list page.
     *
     * @param basePageRequest request object
     */
    @Permissions("crm:signin:index")
    public void getSigninList(BasePageRequest<CrmSignInPageRequest> basePageRequest){
        try{
            renderJson(R.ok().put("data", crmSignInService.querySignInPageList(basePageRequest, BaseUtil.getCrmUser())));
        }catch (Exception e){
            logger.error(String.format("querySignInPageList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    @CrmEventAnnotation(crmEventEnum = CrmEventEnum.LATELY_FOLLOW_EVENT)
    public void addNotesToSignIn() {
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            R r = crmNotesService.addOrUpdate(jsonObject, BaseUtil.getUserId());
            if (r.isSuccess()) {
                r = crmSignInService.updateSignInNotes(jsonObject.getString(CrmConstant.SIGN_IN_HISTORY_ID), r.get(CrmConstant.ADMIN_NOTES_ID_KEY) == null ? "" : r.get(CrmConstant.ADMIN_NOTES_ID_KEY).toString());
            }
            renderJson(r);
        }catch (Exception e){
            logger.error(String.format("addOrUpdate notes error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * Export sign in list.
     */
    @Permissions("crm:signin:export")
    public void exportSigninExcel(BasePageRequest<CrmSignInPageRequest> basePageRequest) {
        try{
            List<Record> records = crmSignInService.querySignInRecords(basePageRequest, BaseUtil.getCrmUser());

            if (CollectionUtils.isEmpty(records)) {
                renderJson(R.ok("no records."));
            }

            List<List<Record>> excelRecords = Lists.newArrayList();
            for(Record record : records) {
                record.set("signinTime", CrmDateUtil.formatDateHours(record.get("signinTime")));
            }
            excelRecords.add(records);

            List<LinkedHashMap<String,String>> headers = Lists.newArrayList();
            headers.add(initExcelHeader());

            ExcelExportUtil.export(headers, excelRecords, CrmConstant.SIGN_IN_EXCEL_NAME, getResponse(),null);

            renderNull();
        }catch (Exception e){
            logger.error(String.format("exportSigninExcel msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    private LinkedHashMap<String,String> initExcelHeader() {
        LinkedHashMap<String,String> headList = new LinkedHashMap<>();
        for(CrmSignInExcelEnum crmSignInExcelEnum : CrmSignInExcelEnum.values()) {
            headList.put(crmSignInExcelEnum.getTypes(),crmSignInExcelEnum.getName());
        }
        return headList;
    }
}
