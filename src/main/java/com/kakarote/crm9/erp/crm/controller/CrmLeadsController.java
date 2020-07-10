package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.annotation.LogApiOperation;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.entity.TagDetails;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmNoteEnum;
import com.kakarote.crm9.erp.crm.common.CrmSensitiveEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.entity.CrmBaseTag;
import com.kakarote.crm9.erp.crm.entity.CrmLeads;
import com.kakarote.crm9.erp.crm.listener.CrmLeadsDataListener;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmLeadsService;
import com.kakarote.crm9.erp.crm.service.CrmNotesService;
import com.kakarote.crm9.erp.crm.service.CrmSensitiveAccessLogService;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Before(IocInterceptor.class)
public class CrmLeadsController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private CrmLeadsService crmLeadsService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    @Autowired
    private NotifyService notifyService;

    @Inject
    private CrmNotesService crmNotesService;

    @Inject
    private CrmSensitiveAccessLogService crmSensitiveAccessLogService;

    @Inject
    private AdminSceneService adminSceneService;

    @Autowired
    private VelocityEngine velocityEngine;

    @Inject
    private AdminDataDicService adminDataDicService;

    @Autowired
    private EsbConfig esbConfig;

    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private CrmCustomerService crmCustomerService;

    /**
     * @author wyq
     * 分页条件查询线索
     */
    public void queryList(BasePageRequest<CrmLeads> basePageRequest){
        try{
            renderJson(R.ok().put("data",crmLeadsService.getLeadsPageList(basePageRequest)));
        }catch (Exception e){
            logger.error(String.format("queryList leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 新增或更新线索queryField
     */
    @Permissions({"crm:leads:save","crm:leads:update"})
    public void addOrUpdate(){
        try{
            JSONObject object= JSON.parseObject(getRawData());
            renderJson(crmLeadsService.addOrUpdate(object,notifyService,velocityEngine,BaseUtil.getUserId()));
        }catch (Exception e){
            logger.error(String.format("addOrUpdate leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 根据线索id查询
     */
    @Permissions("crm:leads:read")
    @NotNullValidate(value = "leadsId",message = "线索id不能为空")
    public void queryById(@Para("leadsId")Integer leadsId){
        try{
            renderJson(R.ok().put("data",crmLeadsService.queryById(leadsId)));
        }catch (Exception e){
            logger.error(String.format("queryById leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 根据线索名称查询
     */
    public void queryByName(@Para("name") String name){
        try{
            renderJson(R.ok().put("data",crmLeadsService.queryByName(name)));
        }catch (Exception e){
            logger.error(String.format("queryByName leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 根据id 删除线索
     */
    @Permissions("crm:leads:delete")
    @NotNullValidate(value = "leadsIds",message = "线索id不能为空")
    public void deleteByIds(@Para("leadsIds")String leadsIds){
        try{
            renderJson(crmLeadsService.deleteByIds(leadsIds));
        }catch (Exception e){
            logger.error(String.format("deleteByIds leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 线索转移
     */
    @Permissions("crm:leads:transfer")
    @NotNullValidate(value = "leadsIds",message = "线索id不能为空")
    @NotNullValidate(value = "newOwnerUserId",message = "新负责人id不能为空")
    public void changeOwnerUser(@Para("leadsIds")String leadsIds,@Para("newOwnerUserId")Integer newOwnerUserId){
        try{
            renderJson(crmLeadsService.updateOwnerUserId(leadsIds,newOwnerUserId));
        }catch (Exception e){
            logger.error(String.format("changeOwnerUser leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 线索转客户
     */
    @Permissions("crm:leads:transform")
    @LogApiOperation(methodName = "线索转客户")
    public void transfer(){
        try{
            JSONObject object= JSON.parseObject(getRawData());
            renderJson(crmLeadsService.translate(object,BaseUtil.getUserId()));
        }catch (Exception e){
            logger.error(String.format("transfer leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 查询自定义字段
     */
    public void queryField(){
        try{
            renderJson(R.ok().put("data",crmLeadsService.queryField()));
        }catch (Exception e){
            logger.error(String.format("queryField leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId",message = "线索id不能为空")
    @NotNullValidate(value = "content",message = "内容不能为空")
    @Permissions("crm:notes:save")
    public void addRecord(@Para("")AdminRecord adminRecord){
        try{
            adminRecord.setCreateUserId(BaseUtil.getUserId().intValue());
            R r = crmNotesService.addRecord(adminRecord, CrmConstant.CRM_LEADS);
            renderJson(r);
        }catch (Exception e){
            logger.error(String.format("addRecord leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    @Permissions("crm:notes:index")
    public void getRecord(BasePageRequest<CrmLeads> basePageRequest){
        try{
            renderJson(R.ok().put("data",crmNotesService.getRecord(basePageRequest, ossPrivateFileUtil, CrmConstant.CRM_LEADS)));
        }catch (Exception e){
            logger.error(String.format("getRecord leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 批量导出线索
     */
    @Permissions("crm:leads:excelexport")
    public void batchExportExcel(@Para("ids")String leadsIds) throws IOException {
        List<Record> recordList = crmLeadsService.exportLeads(leadsIds);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 导出全部线索
     */
    @Permissions("crm:leads:excelexport")
    public void allExportExcel(BasePageRequest basePageRequest) throws IOException{
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel","yes").fluentPut("type","1");
        AdminSceneService adminSceneService = new AdminSceneService();
        List<Record> recordList = (List<Record>)adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList);
        renderNull();
    }

    private void export(List<Record> recordList) throws IOException{
        ExcelWriter writer = ExcelUtil.getWriter();
        AdminFieldService adminFieldService = new AdminFieldService();
        List<Record> fieldList = adminFieldService.list("1");
        writer.addHeaderAlias("leads_name","线索名称");
        writer.addHeaderAlias("next_time","下次联系时间");
        writer.addHeaderAlias("telephone","电话");
        writer.addHeaderAlias("mobile","手机号");
        writer.addHeaderAlias("address","地址");
        writer.addHeaderAlias("remark","备注");
        writer.addHeaderAlias("create_user_name","创建人");
        writer.addHeaderAlias("owner_user_name","负责人");
        writer.addHeaderAlias("create_time","创建时间");
        writer.addHeaderAlias("update_time","更新时间");
        for (Record field:fieldList){
            writer.addHeaderAlias(field.getStr("name"),field.getStr("name"));
        }
        writer.merge(10+fieldList.size(),"线索信息");
        HttpServletResponse response = getResponse();
        List<Map<String,Object>> list = new ArrayList<>();
        for (Record record : recordList){
            list.add(record.remove("batch_id","is_transform","customer_id","leads_id","owner_user_id","create_user_id").getColumns());
        }
        writer.write(list,true);
        for (int i=0; i < fieldList.size()+15;i++){
            writer.setColumnWidth(i,20);
        }
        //自定义标题别名
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        response.setHeader("Content-Disposition", "attachment;filename=leads.xls");
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out);
        // 关闭writer，释放内存
        writer.close();
    }

    /**
     * @author wyq
     * 获取线索导入模板
     */
    public void downloadExcel(){
        HSSFWorkbook wb = new HSSFWorkbook();
        try{
            List<Record> recordList = crmLeadsService.queryExcelField();
            HSSFSheet sheet = wb.createSheet("线索导入表");
            HSSFRow row = sheet.createRow(0);
            for (int i=0;i < recordList.size();i++){
                Record record = recordList.get(i);
                String[] setting = record.get("setting");
                // 在第一行第一个单元格，插入选项
                HSSFCell cell = row.createCell(i);
                // 普通写入操作
                if (record.getInt("is_null") == 1){
                    cell.setCellValue(record.getStr("name")+"(*)");
                }else {
                    cell.setCellValue(record.getStr("name"));
                }
                if (setting != null && setting.length != 0){
                    // 生成下拉列表
                    CellRangeAddressList regions = new CellRangeAddressList(0, Integer.MAX_VALUE, i, i);
                    // 生成下拉框内容
                    DVConstraint constraint = DVConstraint.createExplicitListConstraint(setting);
                    // 绑定下拉框和作用区域
                    HSSFDataValidation dataValidation = new HSSFDataValidation(regions,constraint);
                    // 对sheet页生效
                    sheet.addValidationData(dataValidation);
                }
            }
            HttpServletResponse response = getResponse();

            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=leads_import.xls");
            wb.write(response.getOutputStream());
            wb.close();
            renderNull();
        }catch (Exception e){
            logger.error(String.format("downloadExcel leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }finally {
            try{
                wb.close();
            }catch(Exception e) {
                logger.error(String.format("downloadExcel leads msg:%s",BaseUtil.getExceptionStack(e)));
                renderJson(R.error(e.getMessage()));
            }
        }
    }

    /**
     * 线索导入（新接口）
     * @param file 导入的文件
     * @param repeatHandling 重复信息处理 1 覆盖 2 跳过
     */
    @Permissions("crm:leads:excelimport")
    public void uploadExcel(@Para("file") File file, @Para("repeatHandling") Integer repeatHandling) {
        if (repeatHandling == null){
            renderJson(R.error("参数： repeatHandling 不能为空"));
            return;
        }
        CrmLeadsDataListener crmLeadsDataListener = new CrmLeadsDataListener(repeatHandling,adminDeptService);
        EasyExcel.read(file)
                .headRowNumber(1)
                .registerReadListener(crmLeadsDataListener)
                .sheet().doRead();


        if (crmLeadsDataListener.getHeadList().size() > 0) {
            renderJson(R.error("请使用最新导入模板"));
            return;
        }
        if (StringUtils.isNotBlank(crmLeadsDataListener.getErrMsg())){
            renderJson(R.error(10000,"校验发现不合规数据，请您修改后再导入：\r\n" + crmLeadsDataListener.getErrMsg()));
            return;
        }

        Long userId = BaseUtil.getUserId();
        try {
            crmLeadsService.addOrUpdateForExcel(crmLeadsDataListener.getDataList(),notifyService,velocityEngine,userId);
        } catch (Exception e) {
            logger.error("线索导入异常：" + e.getMessage());
            renderJson(R.error(e.getMessage()));
            return;
        } finally {
            crmLeadsDataListener.clearDataList();
        }

        renderJson(R.ok());
    }

    /**
     * 线索领取
     * @author yue.li
     * @param leadsId 线索ID
     */
    @Permissions("crm:leads:receive")
    @NotNullValidate(value = "leadsId",message = "线索id不能为空")
    public void receive(@Para("leadsId")String leadsId){
        try{
            renderJson(crmLeadsService.receive(leadsId));
        }catch (Exception e){
            logger.error(String.format("receive leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 根据线索ID获取线索场景
     * @author yue.li
     * @param leadsId 线索ID
     */
    @NotNullValidate(value = "leadsId",message = "线索id不能为空")
    public void getSemById(@Para("leadsId")String leadsId){
        try{
            renderJson(R.ok().put("data",crmLeadsService.getSemById(leadsId)));
        }catch (Exception e){
            logger.error(String.format("getSemById leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 线索保护规则设置
     * @author yue.li
     */
    @NotNullValidate(value = "pullDeptPoolContactSubtotalDay",message = "部门线索池无联系小计天数不能为空")
    @NotNullValidate(value = "pullDeptPoolNotTransformDay",message = "部门线索池未转化天数不能为空")
    @NotNullValidate(value = "pullPublicPoolDay",message = "线索公海天数不能为空")
    @NotNullValidate(value = "type",message = "启用状态不能为空")
    public void updateRulesSetting(){
        try{
            //部门线索池无联系小计天数
            Integer  pullDeptPoolContactSubtotalDay =getParaToInt("pullDeptPoolContactSubtotalDay");
            //部门线索池未转化天数
            Integer  pullDeptPoolNotTransformDay =getParaToInt("pullDeptPoolNotTransformDay");
            //线索公海天数
            Integer  pullPublicPoolDay = getParaToInt("pullPublicPoolDay");
            //启用状态
            Integer type = getParaToInt("type");

            renderJson(crmLeadsService.updateRulesSetting(pullDeptPoolContactSubtotalDay,pullDeptPoolNotTransformDay,pullPublicPoolDay,type));
        }catch (Exception e){
            logger.error(String.format("updateRulesSetting leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 获取线索保护规则设置
     * @author yue.li
     *
     */
    public void getRulesSetting(){
        try{
            renderJson(crmLeadsService.getRulesSetting());
        }catch (Exception e){
            logger.error(String.format("getRulesSetting leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }


    /**
     * 线索放入公海
     * @author yue.li
     */
    @Permissions("crm:leads:putinpool")
    public void pullLeadsPublicPool(){
        try{
            JSONObject object= JSON.parseObject(getRawData());
            renderJson(crmLeadsService.pullLeadsPublicPool(object,BaseUtil.getUserId()));
        }catch (Exception e){
            logger.error(String.format("pullLeadsPublicPool leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    public void getTelephoneByLeadsId(@Para("id")String id) {
        try{
            R result = crmLeadsService.getTelephoneByLeadsId(id);
            crmSensitiveAccessLogService.addSensitiveAccessLog(CrmSensitiveEnum.LEADS_TELEPHONE, BaseUtil.getUser().getUsername(), id);
            renderJson(result);
        }catch (Exception e){
            logger.error(String.format("getTelephoneByLeadsId leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    public void getWechatByLeadsId(@Para("id")String id) {
        try{
            R result = crmLeadsService.getWechatByLeadsId(id);
            crmSensitiveAccessLogService.addSensitiveAccessLog(CrmSensitiveEnum.LEADS_WECHAT, BaseUtil.getUser().getUsername(), id);
            renderJson(result);
        }catch (Exception e){
            logger.error(String.format("getWechatByLeadsId leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    public void getEmailByLeadsId(@Para("id")String id) {
        try{
            R result = crmLeadsService.getEmailByLeadsId(id);
            crmSensitiveAccessLogService.addSensitiveAccessLog(CrmSensitiveEnum.LEADS_EMAIL, BaseUtil.getUser().getUsername(), id);
            renderJson(result);
        }catch (Exception e){
            logger.error(String.format("getEmailByLeadsId leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 线索列表页查询
     */
    @Permissions("crm:leads:index")
    public void queryLeadsPageList(BasePageRequest basePageRequest){
        try{
            renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
        }catch (Exception e){
            logger.error(String.format("queryLeadsPageList leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 查询基本信息
     * @author yue.li
     * @param id
     */
    @Permissions("crm:leads:read")
    public void information(@Para("id")Integer id){
        try{
            List<Record> recordList = crmLeadsService.information(id);
            renderJson(R.ok().put("data",recordList));
        }catch (Exception e){
            logger.error(String.format("information leads msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 根据线索ID获取该线索所有标签
     * @author yue.li
     * @param id 线索ID
     */
    public void getLeadTagById(@Para("id")Integer id) {
        try{
            List<Record> resultList = new ArrayList<>();
            List<TagDetails> tagDetailsList = adminDataDicService.getTagDetail(CrmTagConstant.PUBLIC_LEADS_REASION,esbConfig);
            List<Record> recordList = adminDataDicService.queryDataDicNoPageList(tagDetailsList, CrmTagConstant.PUBLIC_LEADS_REASION);
            Record privateRecord = Db.findFirst(Db.getSql("crm.privateTag.queryPrivateTagByTypeAndId"), id, CrmNoteEnum.CRM_LEADS_KEY.getTypes());
            if(recordList != null && recordList.size() >0) {
                for(Record dicRecord: recordList){
                    Record record = new Record();
                    record.set("id",dicRecord.get("id"));
                    record.set("name",dicRecord.get("name"));
                    record.set("isPublic",Boolean.TRUE);
                    resultList.add(record);
                }
            }
            if(privateRecord != null){
                List<CrmBaseTag> privateList = JSON.parseArray(privateRecord.get("content"),CrmBaseTag.class);
                if(privateList != null && privateList.size() >0) {
                    for(CrmBaseTag tag:privateList){
                        if(!tag.isPublic()){
                            Record record = new Record();
                            record.set("id",tag.getId());
                            record.set("name",tag.getName());
                            record.set("isPublic",Boolean.FALSE);
                            resultList.add(record);
                        }
                    }
                }
            }
            renderJson(R.ok().put("data",resultList));
        }catch (Exception e){
            logger.error(String.format("tag getLeadTagById msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 模糊查询客户名称
     * @author yue.li
     * @param company 客户名称
     */
    public void getLeadsListByCompany(@Para("company") String company) {
        renderJson(crmCustomerService.searchCustomerName(company));
    }

    /**
     * 根据行业id获取推荐事业部
     * @author yue.li
     * @param industryId 行业ID
     */
    public void getDeptListByIndustryId(@Para("industryId") String industryId) {
        renderJson(crmLeadsService.getDeptListByIndustryId(industryId));
    }
}
