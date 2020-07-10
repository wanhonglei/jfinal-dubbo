package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.CrmEventAnnotation;
import com.kakarote.crm9.common.annotation.HttpEnum;
import com.kakarote.crm9.common.annotation.LogApiOperation;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.common.CrmEventEnum;
import com.kakarote.crm9.erp.crm.dto.CrmCallRecordDto;
import com.kakarote.crm9.erp.crm.entity.CrmCallContactRelation;
import com.kakarote.crm9.erp.crm.entity.CrmCallRecord;
import com.kakarote.crm9.erp.crm.service.CrmContactRecordService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import com.mysql.jdbc.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 云呼叫中心控制类
 *
 * @author honglei.wan
 */
@Before(IocInterceptor.class)
public class CrmContactRecordController extends Controller {
    private static final Log logger = Log.getLog(CrmContactRecordController.class);


    @Inject
    private CrmContactRecordService crmContactRecordService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    @Inject
    private CrmCustomerService crmCustomerService;

	/**
	 * 获取系统时间
	 */
	public void getSysDate() {
		renderJson(R.okWithData(new Date()));
	}

    /**
     * 通话记录保存接口
     */
    @Permissions("crm:ccc:call")
	@LogApiOperation(methodName = "通话记录保存")
    @NotNullValidate(value = "typesId", message = "typesId 不能为空", type = HttpEnum.JSON)
//    @NotNullValidate(value = "calledName", message = "calledName 不能为空", type = HttpEnum.JSON)
    @NotNullValidate(value = "calledNumber", message = "calledNumber 不能为空", type = HttpEnum.JSON)
    @NotNullValidate(value = "types", message = "types 不能为空", type = HttpEnum.JSON)
    @NotNullValidate(value = "callingUserId", message = "callingUserId 不能为空", type = HttpEnum.JSON)
    @NotNullValidate(value = "callingName", message = "callingName 不能为空", type = HttpEnum.JSON)
	@CrmEventAnnotation(crmEventEnum = CrmEventEnum.LATELY_FOLLOW_EVENT)
    public void contactSave() {
    	try {
			JSONObject jsonObject= JSON.parseObject(getRawData());
			CrmCallRecord crmCallRecord = JSON.parseObject(getRawData(), CrmCallRecord.class);
			if (!"crm_leads".equals(crmCallRecord.getTypes()) && !"crm_customer".equals(crmCallRecord.getTypes())) {
			    renderJson(R.error("参数 types 值非法"));
			    return;
			}

			if (!StringUtils.isNullOrEmpty(crmCallRecord.getFileName()) && !crmCallRecord.getFileName().contains(".")) {
			    renderJson(R.error("参数 fileName 值非法"));
			    return;
			}

			//保证客户姓名为空的时候，可以保存通话记录成功
			if (StringUtils.isNullOrEmpty(crmCallRecord.getCalledName())){
				crmCallRecord.setCalledName("客户");
			}

			crmCallRecord.setId(null);
			BigInteger callRecordId = crmContactRecordService.contactSave(crmCallRecord, 1L);
			if (Objects.nonNull(callRecordId)) {
				Map<String, BigInteger> result = new HashMap<>(1);
				result.put("callRecordId", callRecordId);
				renderJson(R.ok().put("data", result));
			}else {
				renderJson(R.error(String.format("创建通话记录失败: %s", crmCallRecord)));
			}
		} catch (Exception e) {
			logger.error(String.format("contactSave msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
    }

    /**
     * 通话记录查询接口
     *
     * @param basePageRequest request
     */
    @Permissions("crm:ccc:contact_list")
    public void queryContactList(BasePageRequest<CrmCallRecordDto> basePageRequest) {
        try {
			CrmCallRecordDto crmCallRecordDto = basePageRequest.getData();
			if (!"crm_leads".equals(crmCallRecordDto.getTypes()) && !"crm_customer".equals(crmCallRecordDto.getTypes())) {
			    renderJson(R.error("参数 types 值非法"));
			    return;
			}
			if (crmCallRecordDto.getTypesId() == null){
			    renderJson(R.error("参数 typesId 不能为空"));
			    return;
			}
			if (Objects.isNull(crmCallRecordDto.getQueryType()) || (!Integer.valueOf(1).equals(crmCallRecordDto.getQueryType())
			        && !Integer.valueOf(2).equals(crmCallRecordDto.getQueryType()) && !Integer.valueOf(3).equals(crmCallRecordDto.getQueryType()))){
			    renderJson(R.error("参数 queryType 值非法"));
			    return;
			}else if (Integer.valueOf(2).equals(crmCallRecordDto.getQueryType()) && Objects.isNull(crmCallRecordDto.getRecordId())){
			    renderJson(R.error("参数 recordId 值非法"));
			    return;
			}else if (Integer.valueOf(3).equals(crmCallRecordDto.getQueryType()) && Integer.valueOf(1).equals(crmCallRecordDto.getRecordType()) 
					&& Objects.isNull(crmCallRecordDto.getRecordId())){
			    renderJson(R.error("参数 recordId 值非法"));
			    return;
			}

			Page<Record> page = crmContactRecordService.queryContactList(basePageRequest, BaseUtil.getUserId());
			page.setList(this.filterSensitiveInformation(page.getList()));
			renderJson(R.ok().put("data", page));
		} catch (Exception e) {
			logger.error(String.format("queryContactList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
    }    
 
    /**
     * 通话记录查询接口
    *
    * @param basePageRequest request
    */
//   @Permissions("crm:ccc:contact_list")
   public void queryContactListCustomerTab(BasePageRequest<CrmCallRecordDto> basePageRequest) {
       try {
    	   if (!this.checkAuth(basePageRequest)) {
    		   renderJson(R.error("无权访问"));
    		   return;
    	   }
			CrmCallRecordDto crmCallRecordDto = basePageRequest.getData();
			if (!"crm_leads".equals(crmCallRecordDto.getTypes()) && !"crm_customer".equals(crmCallRecordDto.getTypes())) {
			    renderJson(R.error("参数 types 值非法"));
			    return;
			}
			if (crmCallRecordDto.getTypesId() == null){
			    renderJson(R.error("参数 typesId 不能为空"));
			    return;
			}
			if (Objects.isNull(crmCallRecordDto.getQueryType()) || (!Integer.valueOf(1).equals(crmCallRecordDto.getQueryType())
			        && !Integer.valueOf(2).equals(crmCallRecordDto.getQueryType()) && !Integer.valueOf(3).equals(crmCallRecordDto.getQueryType()))){
			    renderJson(R.error("参数 queryType 值非法"));
			    return;
			}else if (Integer.valueOf(2).equals(crmCallRecordDto.getQueryType()) && Objects.isNull(crmCallRecordDto.getRecordId())){
			    renderJson(R.error("参数 recordId 值非法"));
			    return;
			}else if (Integer.valueOf(3).equals(crmCallRecordDto.getQueryType()) && Integer.valueOf(1).equals(crmCallRecordDto.getRecordType()) 
					&& Objects.isNull(crmCallRecordDto.getRecordId())){
			    renderJson(R.error("参数 recordId 值非法"));
			    return;
			}

			Page<Record> page = crmContactRecordService.queryContactList(basePageRequest, BaseUtil.getUserId());
			page.setList(this.filterSensitiveInformation(page.getList()));
			renderJson(R.ok().put("data", page));
		} catch (Exception e) {
			logger.error(String.format("queryContactList msg:%s", BaseUtil.getExceptionStack(e)));
           renderJson(R.error(e.getMessage()));
		}
   }

    /**
     * 小记与通话记录关联接口
     */
    @Permissions("crm:ccc:callcat_related")
    public void contactRecordSave() {
    	Map paramMap = JSON.parseObject(getRawData(), Map.class);
        Integer contactRecordId = (Integer)paramMap.get("contactRecordId");
        if (contactRecordId == null || !String.valueOf(contactRecordId).matches("[0-9]+")){
            renderJson(R.error("参数 contactRecordId 值非法"));
            return;
        }
        List<String> callRecordIdList = Arrays.asList(((String) paramMap.get("callRecordIdList")).split(","));
        if (callRecordIdList.size() == 0){
            renderJson(R.error("参数 callRecordId 值非法"));
            return;
        }

    	// 删除已有数据
    	Db.delete(Db.getSql("crm.contractRecord.deleteCallContactRelationByRecord"),
                contactRecordId);
    	// 组装保存数据
        List<CrmCallContactRelation> relationList = callRecordIdList.stream().map(o -> {
            CrmCallContactRelation crmCallContactRelation = new CrmCallContactRelation();
            crmCallContactRelation.setCallRecordId(Long.valueOf(o));
            crmCallContactRelation.setAdminRecordId(contactRecordId);
            crmCallContactRelation.setOperateUserId(BaseUtil.getUserId());

            return crmCallContactRelation;
        }).collect(Collectors.toList());

        renderJson(Db.batchSave(relationList,relationList.size()).length == relationList.size() ? R.ok() : R.error(String.format("小记关联通话记录失败: %s", relationList)));
    }
    
    /**
     * 通话记录查询接口
     */
    @NotNullValidate(value = "id", message = "id 不能为空", type = HttpEnum.PARA)
    public void queryCalledNumber(@Para("id")Integer id) {
        try {
        	CrmCallRecord crmCallRecord = new CrmCallRecord();
			renderJson(R.ok().put("data", Kv.by("calledNumber", crmCallRecord.findById(id).getCalledNumber())));
		} catch (Exception e) {
			logger.error(String.format("queryCalledNumber msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
    }
    
    /**
     * 去除敏感邮件信息，其他信息格式化显示
     * @param records 记录列表
     * @return
     */
    private List<Record> filterSensitiveInformation(List<Record> records) {
        if (records != null && records.size() > 0) {
            records.forEach(item -> {

                String phone = item.getStr("called_number");
                if (org.apache.commons.lang3.StringUtils.isNoneBlank(phone) && phone.length() > 7){
                	phone = phone.substring(0,3) + "****" + phone.substring(phone.length() - 4);
                    item.remove("called_number");
                    item.set("called_number", phone);
                }


                String mobile = item.getStr("calledNumber");
                if (org.apache.commons.lang3.StringUtils.isNoneBlank(mobile) && mobile.length() > 7){
                    mobile = mobile.substring(0,3) + "****" + mobile.substring(mobile.length() - 4);
                    item.remove("calledNumber");
                    item.set("calledNumber", mobile);
                }

            });
        }
        return records;
    }
 
    /**
     * 判断是否是客户团队成员
     * @param basePageRequest
     * @return
     */
    public boolean checkAuth(BasePageRequest<CrmCallRecordDto> basePageRequest) {
    	Integer customerId = basePageRequest.getData().getTypesId();
    	Record r =  crmCustomerService.queryById(customerId,ossPrivateFileUtil);
    	if (ObjectUtil.isNotNull(r) && !StringUtils.isNullOrEmpty(r.getStr("owner_user_id"))){
    		List<String> authUserIds = new ArrayList<>();
    		authUserIds.add(r.getStr("owner_user_id"));
    		if (!StringUtils.isNullOrEmpty(r.getStr("ro_user_id"))) {
    			authUserIds.addAll(Arrays.asList(r.getStr("ro_user_id").split(",")));
    		}
    		// 去除空元素
    		authUserIds.removeIf(userId -> userId == null ||"".equals(userId));
    		return authUserIds.contains(String.valueOf(BaseUtil.getUserId()));
    	}
    	return false;
    }
}
