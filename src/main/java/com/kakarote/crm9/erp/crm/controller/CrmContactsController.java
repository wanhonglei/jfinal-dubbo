package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
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
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmSensitiveEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmContactsBusiness;
import com.kakarote.crm9.erp.crm.service.CrmContactsService;
import com.kakarote.crm9.erp.crm.service.CrmNotesService;
import com.kakarote.crm9.erp.crm.service.CrmSensitiveAccessLogService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Before(IocInterceptor.class)
public class CrmContactsController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private CrmContactsService crmContactsService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    @Inject
    private CrmNotesService crmNotesService;

    @Inject
    private CrmSensitiveAccessLogService crmSensitiveAccessLogService;

    @Inject
    private AdminSceneService adminSceneService;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private VelocityEngine velocityEngine;
    /**
     * @author wyq
     * 分页条件查询联系人
     */
    public void queryList(BasePageRequest<CrmContacts> basePageRequest){
        try{
            renderJson(R.ok().put("data",crmContactsService.queryList(basePageRequest)));
        }catch (Exception e){
            logger.error(String.format("queryList contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 根据id查询联系人
     */
    @Permissions("crm:contacts:read")
    public void queryById(@Para("contactsId")Integer contactsId){
        try{
            renderJson(R.ok().put("data",crmContactsService.queryById(contactsId)));
        }catch (Exception e){
            logger.error(String.format("queryById contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 根据联系人名称查询
     */
    public void queryByName(@Para("name")String name){
        try{
            renderJson(R.ok().put("data",crmContactsService.queryByName(name)));
        }catch (Exception e){
            logger.error(String.format("queryByName contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 根据联系人id查询商机
     */
    public void queryBusiness(BasePageRequest<CrmContacts> basePageRequest){
        try{
            renderJson(crmContactsService.queryBusiness(basePageRequest));
        }catch (Exception e){
            logger.error(String.format("queryBusiness contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 联系人关联商机
     */
    public void relateBusiness(@Para("")CrmContactsBusiness crmContactsBusiness){
        try{
            renderJson(crmContactsService.relateBusiness(crmContactsBusiness));
        }catch (Exception e){
            logger.error(String.format("queryBusiness contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 联系人解除关联商机
     */
    public void unrelateBusiness(@Para("id")Integer id){
        renderJson(crmContactsService.unrelateBusiness(id));
    }

    /**
     * @author wyq
     * 新建或更新联系人
     */
    @Permissions({"crm:contacts:save","crm:contacts:update"})
    public void addOrUpdate(){
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            renderJson(crmContactsService.addOrUpdate(jsonObject, BaseUtil.getUserId()));
        }catch (Exception e){
            logger.error(String.format("addOrUpdate contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 根据id删除联系人
     */
    @Permissions("crm:contacts:delete")
    public void deleteByIds(@Para("contactsIds")String contactsIds){
        try{
            renderJson(crmContactsService.deleteByIds(contactsIds));
        }catch (Exception e){
            logger.error(String.format("deleteByIds contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 联系人转移
     */
    @Permissions("crm:contacts:transfer")
    @NotNullValidate(value = "contactsIds",message = "联系人id不能为空")
    @NotNullValidate(value = "newOwnerUserId",message = "新负责人不能为空")
    public void transfer(@Para("")CrmContacts crmContacts){
        try{
            renderJson(crmContactsService.transfer(crmContacts));
        }catch (Exception e){
            logger.error(String.format("transfer contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 查询自定义字段
     */
    public void queryField(){
        renderJson(R.ok().put("data",crmContactsService.queryField()));
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId",message = "联系人id不能为空")
    @NotNullValidate(value = "content",message = "内容不能为空")
    @Permissions("crm:notes:save")
    public void addRecord(@Para("")AdminRecord adminRecord){
        try{
            adminRecord.setCreateUserId(BaseUtil.getUserId().intValue());
            R r = crmNotesService.addRecord(adminRecord, CrmConstant.CRM_CONTACTS);
            renderJson(r);
        }catch (Exception e){
            logger.error(String.format("addRecord contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    @Permissions("crm:notes:index")
    public void getRecord(BasePageRequest<CrmContacts> basePageRequest){
        try{
            renderJson(R.ok().put("data",crmNotesService.getRecord(basePageRequest, ossPrivateFileUtil, CrmConstant.CRM_CONTACTS)));
        }catch (Exception e){
            logger.error(String.format("getRecord contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 批量导出线索
     */
    @Permissions("crm:contacts:excelexport")
    public void batchExportExcel(@Para("ids")String contactsIds) throws IOException {
        List<Record> recordList = crmContactsService.exportContacts(contactsIds);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 导出全部联系人
     */
    @Permissions("crm:contacts:excelexport")
    public void allExportExcel(BasePageRequest basePageRequest) throws IOException{
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel","yes").fluentPut("type","3");
        AdminSceneService adminSceneService = new AdminSceneService();
        List<Record> recordList = (List<Record>)adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList);
        renderNull();
    }

    private void export(List<Record> recordList) throws IOException{
        int column = 13;
        ExcelWriter writer = ExcelUtil.getWriter();
        AdminFieldService adminFieldService = new AdminFieldService();
        adminFieldService.list("3");
        writer.addHeaderAlias("name","联系人姓名");
        writer.addHeaderAlias("customer_name","所属客户");
        writer.addHeaderAlias("post","职务");
        writer.addHeaderAlias("mobile","手机");
        writer.addHeaderAlias("email","邮箱");
        writer.addHeaderAlias("telephone","办公电话");
        writer.addHeaderAlias("wechat","微信");
        writer.addHeaderAlias("role","角色");
        writer.addHeaderAlias("attitude","态度");
        writer.addHeaderAlias("hobby","兴趣爱好");
        writer.addHeaderAlias("remark","备注");
        writer.addHeaderAlias("create_user_name","创建人");
        writer.addHeaderAlias("create_time","创建时间");
        writer.addHeaderAlias("update_time","更新时间");
//        for (Record field:fieldList){
//            writer.addHeaderAlias(field.getStr("name"),field.getStr("name"));
//        }

//        writer.merge(12+fieldList.size(),"联系人信息");
        writer.merge(column,"联系人信息");
        HttpServletResponse response = getResponse();
        List<Map<String,Object>> list = new ArrayList<>(recordList.size());
        for (Record record : recordList){

            list.add(record.remove("batch_id","contacts_name","customer_id","contacts_id","owner_user_id",
                    "create_user_id","address","next_time","owner_user_name","是否关键决策人","性别").getColumns());
        }
        writer.write(list,true);
        for (int i=0; i < column+1;i++){
            writer.setColumnWidth(i,20);
        }
        //自定义标题别名
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        response.setHeader("Content-Disposition", "attachment;filename=contacts.xls");
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out);
        // 关闭writer，释放内存
        writer.close();
    }

    /**
     * @author wyq
     * 获取联系人导入模板
     */
    public void downloadExcel(){
        List<Record> recordList = crmContactsService.queryField();
//        recordList.removeIf(record -> record.getInt("type")>=8 || record.getInt("type")<=12);
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("联系人导入表");
        HSSFRow row = sheet.createRow(0);
        List<String> customerList = Db.query("select customer_name from 72crm_crm_customer");
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
            if ("客户名称".equals(record.getStr("name"))){
                setting = customerList.toArray(new String[customerList.size()]);
            }
            if (setting.length != 0){
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
        try {
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=contacts_import.xls");
            wb.write(response.getOutputStream());
            wb.close();
        } catch (Exception e) {
            renderJson(R.error(e.getMessage()));
        }finally{
            try{
                wb.close();
            }catch(Exception e) {
                logger.error(String.format("downloadExcel contacts msg:%s",BaseUtil.getExceptionStack(e)));
            }
        }
        renderNull();
    }

    /**
     * @author wyq
     * 联系人导入
     */
    @Permissions("crm:contacts:excelimport")
    public void uploadExcel(@Para("file") UploadFile file){
        try{
            renderJson(crmContactsService.uploadExcel(file, BaseUtil.getUserId()));
        }catch (Exception e){
            logger.error(String.format("getRecord contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    public void getTelephoneByContactsId(@Para("id")String id) {
        try{
            R result = crmContactsService.getTelephoneByContactsId(id);
            crmSensitiveAccessLogService.addSensitiveAccessLog(CrmSensitiveEnum.CONTACTS_TELEPHONE, BaseUtil.getUser().getUsername(), id);
            renderJson(result);
        }catch (Exception e){
            logger.error(String.format("getTelephoneByContactsId contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    public void getMobileByContactsId(@Para("id")String id) {
        try{
            R result =crmContactsService.getMobileByContactsId(id);
            crmSensitiveAccessLogService.addSensitiveAccessLog(CrmSensitiveEnum.CONTACTS_MOBILE, BaseUtil.getUser().getUsername(), id);
            renderJson(result);
        }catch (Exception e){
            logger.error(String.format("getMobileByContactsId contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    public void getWechatByContactsId(@Para("id")String id) {
        try{
            R result = crmContactsService.getWechatByContactsId(id);
            crmSensitiveAccessLogService.addSensitiveAccessLog(CrmSensitiveEnum.CONTACTS_WECHAT, BaseUtil.getUser().getUsername(), id);
            renderJson(result);
        }catch (Exception e){
            logger.error(String.format("getWechatByContactsId contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    public void getEmailByContactsId(@Para("id")String id) {
        try{
            R result = crmContactsService.getEmailByContactsId(id);
            crmSensitiveAccessLogService.addSensitiveAccessLog(CrmSensitiveEnum.CONTACTS_EMAIL, BaseUtil.getUser().getUsername(), id);
            renderJson(result);
        }catch (Exception e){
            logger.error(String.format("getEmailByContactsId contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询基本信息
     * @author yue.li
     * @param id
     */
    @Permissions("crm:contacts:read")
    public void information(@Para("id")Integer id){
        try{
            List<Record> recordList= crmContactsService.information(id);
            renderJson(R.ok().put("data",recordList));
        }catch (Exception e){
            logger.error(String.format("information contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 联系人列表页查询
     */
    @Permissions("crm:contacts:index")
    public void queryContactsPageList(BasePageRequest basePageRequest){
        try{
            renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
        }catch (Exception e){
            logger.error(String.format("queryContactsPageList contacts error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据手机号校验分销商
     * @param phone
     */
    @NotNullValidate(value = "phone",message = "手机号不能为空")
    public void checkDistributor(String phone) {
        try {
            crmContactsService.checkDistributor(phone);
            renderJson(R.ok());
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }
}
