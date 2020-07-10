package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.erp.admin.entity.AdminFieldSort;
import com.kakarote.crm9.erp.admin.entity.AdminFieldStyle;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.erp.crm.service.CrmContactsService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmLeadsService;
import com.kakarote.crm9.erp.crm.service.CrmProductService;
import com.kakarote.crm9.erp.crm.service.CrmReceivablesPlanService;
import com.kakarote.crm9.erp.crm.service.CrmReceivablesService;
import com.kakarote.crm9.utils.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author hmb
 */
public class AdminFieldController extends Controller {

    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmBusinessService crmBusinessService;

    @Inject
    private CrmContactsService crmContactsService;

    @Inject
    private CrmLeadsService crmLeadsService;

    @Inject
    private CrmProductService crmProductService;

    @Inject
    private CrmReceivablesService crmReceivablesService;

    @Inject
    private CrmReceivablesPlanService crmReceivablesPlanService;

    /**
     * @author zhangzhiwei
     * 保存自定义字段E
     */
    public void save() {
        String str=getRawData();
        JSONObject jsonObject= JSON.parseObject(str);
        renderJson(adminFieldService.save(jsonObject));
    }

    /**
     *
     */
    public void queryFields(){
        renderJson(adminFieldService.queryFields());
    }
    /**
     * @author zxy
     * 查询自定义字段列表
     */
    public void list(){
        JSONObject object=JSONObject.parseObject(getRawData());
        renderJson(R.ok().put("data",adminFieldService.list(object.getString("label"),object.getString("categoryId"))));
    }

    /**
     * @author wyq
     * 查询新增或编辑字段
     */
    public void queryField(@Para("label")String label,@Para("id")Integer id){
        List<Record> recordList = new LinkedList<>();
        if (id != null){
            if (CrmEnum.LEADS_TYPE_KEY.getTypes().equals(label)){
                recordList = crmLeadsService.queryField(id);
            }
            if (CrmEnum.CUSTOMER_TYPE_KEY.getTypes().equals(label)){
                recordList = crmCustomerService.queryField(id);
            }
            if (CrmEnum.CONTACTS_TYPE_KEY.getTypes().equals(label)){
                recordList = crmContactsService.queryField(id);
            }
            if (CrmEnum.PRODUCT_TYPE_KEY.getTypes().equals(label)){
                recordList = crmProductService.queryField(id);
            }
            if (CrmEnum.BUSINESS_TYPE_KEY.getTypes().equals(label)){
                recordList = crmBusinessService.queryField(id);
            }
            if (CrmEnum.RECEIVABLES_TYPE_KEY.getTypes().equals(label)){
                recordList = crmReceivablesService.queryField(id);
            }
            if(CrmEnum.ADMIN_FIELD_TYPE_KEY.getTypes().equals(label)){
                recordList = adminFieldService.list(CrmEnum.ADMIN_FIELD_TYPE_KEY.getTypes(),id.toString());
            }
            if (CrmEnum.RECEIVABLES_PLAN_TYPE_KEY.getTypes().equals(label)){
                recordList = crmReceivablesPlanService.queryField(id);
            }
        }else {
            if (CrmEnum.LEADS_TYPE_KEY.getTypes().equals(label)) {
                recordList = crmLeadsService.queryField();
            }
            if (CrmEnum.CUSTOMER_TYPE_KEY.getTypes().equals(label)) {
                recordList = crmCustomerService.queryField();
            }
            if (CrmEnum.CONTACTS_TYPE_KEY.getTypes().equals(label)) {
                recordList = crmContactsService.queryField();
            }
            if (CrmEnum.PRODUCT_TYPE_KEY.getTypes().equals(label)) {
                recordList = crmProductService.queryField();
            }
            if (CrmEnum.BUSINESS_TYPE_KEY.getTypes().equals(label)) {
                recordList = crmBusinessService.queryField();
            }
            if (CrmEnum.RECEIVABLES_TYPE_KEY.getTypes().equals(label)) {
                recordList = crmReceivablesService.queryField();
            }
            if (CrmEnum.RECEIVABLES_PLAN_TYPE_KEY.getTypes().equals(label)) {
                recordList = crmReceivablesPlanService.queryField();
            }
        }
        renderJson(R.ok().put("data",recordList));
    }

    /**
     * @author wyq
     * @param types 模块类型
     * @param id
     * 查询基本信息
     */
    public void information(@Para("types")Integer types,@Para("id")Integer id){
        List<Record> recordList;
        if (Integer.parseInt(CrmEnum.LEADS_TYPE_KEY.getTypes()) == types){
            recordList = crmLeadsService.information(id);
        }else if (Integer.parseInt(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()) == types){
            recordList = crmCustomerService.information(id);
        }else if (Integer.parseInt(CrmEnum.CONTACTS_TYPE_KEY.getTypes()) == types){
            recordList = crmContactsService.information(id);
        }else if (Integer.parseInt(CrmEnum.PRODUCT_TYPE_KEY.getTypes()) == types){
            recordList = crmProductService.information(id);
        }else if (Integer.parseInt(CrmEnum.BUSINESS_TYPE_KEY.getTypes()) == types){
            recordList = crmBusinessService.information(id);
        }else if (Integer.parseInt(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes()) == types){
            recordList = crmReceivablesService.information(id);
        }else {
            recordList=new ArrayList<>();
        }

        renderJson(R.ok().put("data",recordList));
    }
    /**
     * @author zhangzhiwei
     * 设置字段样式
     */
    public void setFelidStyle(@Para("") AdminFieldStyle adminFleldStyle){
        renderJson(adminFieldService.setFelidStyle(getKv()));
    }
    /**
     * @author zhangzhiwei
     * 验证字段数据
     */
    @NotNullValidate(value = "val",message = "字段校验参数错误")
    @NotNullValidate(value = "types",message = "字段校验参数错误")
    @NotNullValidate(value = "name",message = "字段校验参数错误")
    public void verify(){
        renderJson(adminFieldService.verify(getKv()));
    }

    /**
     * @author wyq
     * 查询客户管理列表页字段
     */
    @NotNullValidate(value = "label",message = "label不能为空")
    public void queryListHead(@Para("") AdminFieldSort adminFieldSort) {
        List<Record> records = adminFieldService.queryListHead(adminFieldSort);
        List<AdminFieldStyle> fieldStyles = adminFieldService.queryFieldStyle(adminFieldSort.getLabel().toString());
        records.forEach(record -> {
            for (AdminFieldStyle fieldStyle : fieldStyles) {
                if (record.get("fieldName") != null && fieldStyle.getFieldName().equals(record.get("fieldName"))) {
                    record.set("width", fieldStyle.getStyle());
                    break;
                }
            }
            if (!record.getColumns().containsKey("width")) {
                record.set("width", 100);
            }
        });
        renderJson(R.ok().put("data", records));
    }

    /**
     * @author wyq
     * 查询字段排序隐藏设置
     */
    @NotNullValidate(value = "label",message = "label不能为空")
    public void queryFieldConfig(@Para("")AdminFieldSort adminFieldSort){
        renderJson(adminFieldService.queryFieldConfig(adminFieldSort));
    }

    /**
     * @author wyq
     * 设置字段排序隐藏
     */
    @NotNullValidate(value = "label",message = "label不能为空")
    @NotNullValidate(value = "noHideIds",message = "显示列不能为空")
    public void fieldConfig(@Para("")AdminFieldSort adminFieldSort){
        renderJson(adminFieldService.fieldConfig(adminFieldSort));
    }

    /**
     * @author wyq
     * 获取导入查重字段
     */
    public void getCheckingField(@Para("type")Integer type){
        R data;
        switch (type) {
            case 1:
                data = crmLeadsService.getCheckingField();
                break;
            case 2:
                data = crmCustomerService.getCheckingField();
                break;
            case 3:
                data = crmContactsService.getCheckingField();
                break;
            case 4:
                data = crmProductService.getCheckingField();
                break;
            default:
                data = R.error("type不符合要求");
        }
        renderJson(data);
    }
}
