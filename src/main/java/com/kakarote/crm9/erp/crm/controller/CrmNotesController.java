package com.kakarote.crm9.erp.crm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.shade.io.netty.util.internal.StringUtil;
import com.google.common.collect.Lists;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.CrmEventAnnotation;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.crm.common.CrmEventEnum;
import com.kakarote.crm9.erp.crm.common.CrmNoteEnum;
import com.kakarote.crm9.erp.crm.common.CrmNoteExcelEnum;
import com.kakarote.crm9.erp.crm.common.CrmSaleUsualEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmCallContactRelation;
import com.kakarote.crm9.erp.crm.service.CrmNotesService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.ExcelExportUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Crm联系小计模块
 */
@Before(IocInterceptor.class)
public class CrmNotesController extends Controller {

    @Inject
    private CrmNotesService crmNotesService;
    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;
    
    private static final Object lock = new Object();

    private Log logger = Log.getLog(getClass());
    /**
     * 添加联系小计
     *
     * @author chaokun.ding
     */
    @Permissions({"crm:notes:save","crm:notes:update"})
    public void addOrUpdate() {
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            R r= crmNotesService.addOrUpdate(jsonObject, BaseUtil.getUserId());
            renderJson(r);
        }catch (Exception e){
            logger.error(String.format("addOrUpdate notes error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 添加联系小计，通话记录可选填
     *
     */
    @Permissions({"crm:notes:save","crm:notes:update"})
    @CrmEventAnnotation(crmEventEnum = CrmEventEnum.LATELY_FOLLOW_EVENT)
    public void addOrUpdateContainCallRecord() {
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            Db.tx(()-> {
            	R result= crmNotesService.addOrUpdate(jsonObject, BaseUtil.getUserId());
                
                AdminRecord adminRecord = jsonObject.toJavaObject(AdminRecord.class);
                List<String> callRecordIdList = StringUtil.isNullOrEmpty(adminRecord.getCallRecordIdList()) ? Lists.newArrayList() : Arrays.asList(adminRecord.getCallRecordIdList().split(","));
                if (Objects.nonNull(result)&&result.isSuccess()&&Objects.nonNull(result.get("notesId"))) {
    	        	Long customerNo = (Long)(result.get("notesId"));
    	        	int contactRecordId = customerNo.intValue();
    	        	
    	        	List<CrmCallContactRelation> currentRelation = CrmCallContactRelation.dao.find(Db.getSql("crm.contractRecord.getCallContactRelationByRecord"), contactRecordId);
    	        	List<Long> currentCallRecordIds = currentRelation.stream().map(CrmCallContactRelation::getCallRecordId).collect(Collectors.toList());

    	            // 添加synchronized锁，保证批量保存串行执行(后续根据线上情况考虑，改成分布式锁)
    	            synchronized(lock) {
    	            	boolean isOK = true;
    	            	// 1、保存新增通话记录
    	            	for (String o : callRecordIdList) {
	    	            	if (!currentCallRecordIds.contains(Long.valueOf(o))) {
	        	                CrmCallContactRelation crmCallContactRelation = new CrmCallContactRelation();
	        	                crmCallContactRelation.setCallRecordId(Long.valueOf(o));
	        	                crmCallContactRelation.setAdminRecordId(contactRecordId);
	        	                crmCallContactRelation.setOperateUserId(BaseUtil.getUserId());
	        	                if (!crmCallContactRelation.save()) {
	        	                	isOK = false;
	        	                	break;
	        	                }
	    	            	}
    	            	}
	    	            
	    	            // 2、删除勾掉的通话记录
    	            	List<Long> longcallRecordIdList = callRecordIdList.stream().map(str-> Long.valueOf(str)).collect(Collectors.toList());
    	            	for (CrmCallContactRelation o : currentRelation) {
	    	            	if (!longcallRecordIdList.contains(o.getCallRecordId())) {
	    	            		if (!o.delete()) {
	    	            			isOK = false;
	    	            			break;
	    	            		}
	    	            	}
	    	            }
    	            	if (!isOK) {
    	            		// 失败时回滚
    	            		renderJson(R.error("关联通话记录失败"));
    	            		return false;
    	            	}
    	            }
    	        }
                renderJson(result);
                return true;
            });
            
        }catch (Exception e){
            logger.error(String.format("addOrUpdateContainCallRecord notes error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 分页条件查询联系小计
     *
     * @author chaokun.ding
     */
    @Permissions("crm:notes:index")
    public void queryList(BasePageRequest<AdminRecord> basePageRequest) {
        try{
            Page<Record> recordList = crmNotesService.queryList(basePageRequest, ossPrivateFileUtil,true);
            renderJson(R.ok().put("data", recordList));
        }catch (Exception e){
            logger.error(String.format("queryList notes error msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据联系小计id删除
     *
     * @param recordId 日志ID
     * @author chaokun.ding
     */
    @Permissions("crm:notes:deletenote")
    public void deleteById(@Para("logId") Integer recordId) {
        try{
            boolean delete = crmNotesService.deleteById(recordId, ossPrivateFileUtil, BaseUtil.getUser());
            renderJson( delete ? R.ok() : R.error("删除失败"));
        }catch (Exception e){
            logger.error(String.format("deleteById notes error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 联系小计导出
     *
     * @param basePageRequest 请求对象
     * @author yue.li
     */
    public void exportNotesReportExcel(BasePageRequest<AdminRecord> basePageRequest) throws Exception{
        Page<Record> recordList = crmNotesService.queryList(basePageRequest, ossPrivateFileUtil,false);
        List<LinkedHashMap<String,String>> headAllList = new ArrayList<>();
        headAllList.add(initHead());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Record record:recordList.getList()){
            record.set(CrmNoteExcelEnum.NOTE_TYPE_KEY.getTypes(),formatType(record.getStr("types")));
            record.set(CrmNoteExcelEnum.CREATE_TIME_KEY.getTypes(),record.getStr("update_time") == null ? null : sdf.format(sdf.parse(record.getStr("update_time"))));
            record.set(CrmNoteExcelEnum.NOTE_NAME_KEY.getTypes(),getNameByType(record.getStr("types"),record));
            record.set(CrmNoteExcelEnum.CATEGORY_KEY.getTypes(),formatCategory(record.getStr("category")));
        }
        List<List<Record>> resultAllList = new ArrayList<>();
        resultAllList.add(recordList.getList());
        ExcelExportUtil.export(headAllList,resultAllList, CrmConstant.NOTE_REPORT,getResponse(),null);
        renderNull();
    }

    /***
     * 初始化表头
     * @author yue.li
     * @return
     */
    public LinkedHashMap<String,String> initHead() {
        LinkedHashMap<String,String> headList = new LinkedHashMap<>();
        headList.put(CrmNoteExcelEnum.NOTE_TYPE_KEY.getTypes(),CrmNoteExcelEnum.NOTE_TYPE_KEY.getName());
        headList.put(CrmNoteExcelEnum.NOTE_NAME_KEY.getTypes(),CrmNoteExcelEnum.NOTE_NAME_KEY.getName());
        headList.put(CrmNoteExcelEnum.ADDRESS_NAME_KEY.getTypes(),CrmNoteExcelEnum.ADDRESS_NAME_KEY.getName());
        headList.put(CrmNoteExcelEnum.CREATE_TIME_KEY.getTypes(),CrmNoteExcelEnum.CREATE_TIME_KEY.getName());
        headList.put(CrmNoteExcelEnum.CREATE_USER_NAME_KEY.getTypes(),CrmNoteExcelEnum.CREATE_USER_NAME_KEY.getName());
        headList.put(CrmNoteExcelEnum.CATEGORY_KEY.getTypes(),CrmNoteExcelEnum.CATEGORY_KEY.getName());
        headList.put(CrmNoteExcelEnum.CONTENT_KEY.getTypes(),CrmNoteExcelEnum.CONTENT_KEY.getName());
        return headList;
    }

    /***
     * 格式化小计类型名称
     * @param type 类型
     * @author yue.li
     * @return
     */
    public String formatType(String type) {
        String noteTypeName = null;
        for(CrmNoteEnum noteEnum: CrmNoteEnum.values()){
            if(StringUtils.isNotEmpty(type) && type.equals(noteEnum.getTypes())){
                noteTypeName = noteEnum.getName();
                break;
            }
        }
        return noteTypeName;
    }

    /***
     * 根据小计类型找到相应的客户、线索、商机、联系人
     * @param type 类型
     * @author yue.li
     * @return
     */
    public String getNameByType(String type,Record record) {
        String name = "";
        if(StringUtils.isNotEmpty(type) && type.equals(CrmNoteEnum.CRM_BUSINESS_KEY.getTypes())) {
            ArrayList<Record> recordList = record.get("businessList");
            if(recordList != null && recordList.size() >0){
                name = recordList.get(0).get("business_name");
            }
        }
        if(StringUtils.isNotEmpty(type) && type.equals(CrmNoteEnum.CRM_LEADS_KEY.getTypes())) {
            ArrayList<Record> recordList = record.get("leadsList");
            if(recordList != null && recordList.size() >0){
                name = recordList.get(0).get("contact_user");
            }
        }
        if(StringUtils.isNotEmpty(type) && type.equals(CrmNoteEnum.CRM_CUSTOMER_KEY.getTypes())) {
            ArrayList<Record> recordList = record.get("customerList");
            if(recordList != null && recordList.size() >0){
                name = recordList.get(0).get("customer_name");
            }
        }
        if(StringUtils.isNotEmpty(type) && type.equals(CrmNoteEnum.CRM_CONTACTS_KEY.getTypes())) {
            ArrayList<Record> recordList = record.get("contactsList");
            if(recordList != null && recordList.size() >0){
                name = recordList.get(0).get("name");
            }
        }
        return name;
    }

    /***
     * 格式化联系方式
     * @param category 联系方式
     * @author yue.li
     * @return
     */
    public String formatCategory(String category) {
        String categoryName = null;
        for(CrmSaleUsualEnum saleUsualEnum: CrmSaleUsualEnum.values()){
            if(StringUtils.isNotEmpty(category) && category.equals(saleUsualEnum.getTypes())){
                categoryName = saleUsualEnum.getName();
                break;
            }
        }
        return categoryName;
    }

    /**
     * Get note by note id.
     *
     * @param noteId
     */
    @NotNullValidate(value = "noteId",message = "联系小记ID不能为空")
    public void getNoteDetail(@Para("noteId") Integer noteId) {
        try{
            renderJson(R.ok().put("data", crmNotesService.getNoteById(noteId, ossPrivateFileUtil)));
        }catch (Exception e){
            logger.error(String.format("getNoteDetail error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
}
