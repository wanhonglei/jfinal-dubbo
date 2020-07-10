package com.kakarote.crm9.erp.crm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.entity.CrmLeads;
import com.kakarote.crm9.utils.BaseUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2019/12/27 14:10
 */
public class CrmLeadsDataListener extends AnalysisEventListener<Map<Integer, String>> {
    private static final Log logger = Log.getLog(CrmLeadsDataListener.class);

    private static List<String> headList = new ArrayList<>(16);

    private AdminDeptService adminDeptService;
    private int repeatHandling;

    private List<JSONObject> dataList = new ArrayList<>(5000);
    private Set<Map.Entry<Integer, String>> entrySet = null;
    private int errorCount, rowCount = 1;
    private StringBuilder errorMsgSb = new StringBuilder();
    private Integer loginDeptId;
    private static Map<String,String> deptMap,tagMap;
    private static Map<String,Integer> userMap;
    private Map<Integer, String> headMap;
    private StringBuilder addressSb = new StringBuilder();
    private Set<String> phoneSet = new HashSet<>(500), companyNameSet = new HashSet<>(500);

    public CrmLeadsDataListener(int repeatHandling,AdminDeptService adminDeptService){
        this.repeatHandling = repeatHandling;
        this.adminDeptService = adminDeptService;
    }

    static {
        headList.add("联系人(*)");
        headList.add("公司");
        headList.add("联系电话");
        headList.add("所在部门");
        headList.add("职位");
        headList.add("邮箱");
        headList.add("微信");
        headList.add("精度需求");
        headList.add("负责人(邮箱前缀)");
        headList.add("推荐事业部");
        headList.add("需求描述(*)");
        headList.add("省");
        headList.add("市");
        headList.add("区");
        headList.add("详细地址");
        headList.add("公司规模");
        headList.add("行业");

        //加载部门树  dept_id, name,dept_level,leader_id,pid
        deptMap = Db.find(Db.getSql("admin.dept.queryDeptList")).stream().collect(Collectors.toMap(o1 -> o1.getStr("name"), o1 -> o1.getStr("dept_id"), (key1, key2)->key1));
        //获取标签名称对应的值
        List<String> tagNameList = new ArrayList<>();
        tagNameList.add(CrmTagConstant.ACCURACY_REQUIREMENTS);
        tagNameList.add(CrmTagConstant.CUSTOMER_GRADE);
        tagNameList.add(CrmTagConstant.CUSTOMER_INDUSTRY);
        tagMap = Db.find(Db.getSqlPara("admin.dataDic.queryDataDicListByTagNameList", Kv.by("tagNameList",tagNameList)))
                .stream().collect(Collectors.toMap(o1 -> o1.getStr("name"), o1 -> o1.getStr("label"), (key1, key2)->key1));
        //加载人员树
        userMap = Db.find(Db.getSql("admin.user.getAllUsers")).stream().collect(Collectors.toMap(o1 -> o1.getStr("userName"), o1 -> o1.getInt("userId"), (key1, key2)->key1));
    }

    /**
     * 这里会一行行的返回头
     * @param headMap 头
     * @param context 上下文
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
        headMap.entrySet().iterator().forEachRemaining(o -> headList.remove(o.getValue()));

        if(headList.size() > 0){
            return;
        }
        //头对应列名和列index
        entrySet = headMap.entrySet();
        //加载登陆人所属事业部id
        loginDeptId = Integer.valueOf(adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(BaseUtil.getDeptId())));

    }

    @Override
    public void invoke(Map<Integer, String> map, AnalysisContext context) {

        if (headList.size() > 0 || errorCount >= 20){
            return;
        }

        String[] addressArray = new String[3];
        CrmLeads[] leadsArray = new CrmLeads[2];
        leadsArray[0] = new CrmLeads();
        Record[] records = new Record[1];

        rowCount++;
        entrySet.forEach(o -> {
            String columnValue = map.get(o.getKey());
            if ("联系人(*)".equals(o.getValue())){
                if (StringUtils.isBlank(columnValue)){
                    errorMsgSb.append("第[");
                    errorMsgSb.append(rowCount);
                    errorMsgSb.append("]行,");
                    errorMsgSb.append("联系人名称不能为空");
                    errorMsgSb.append("\r\n");
                    errorCount++;
                }else {
                    leadsArray[0].setContactUser(columnValue);
                }
            }

            if("推荐事业部".equals(o.getValue()) && StringUtils.isNotBlank(columnValue)){
                String deptId = deptMap.get(columnValue);
                if (deptId == null){
                    errorMsgSb.append("第[");
                    errorMsgSb.append(rowCount);
                    errorMsgSb.append("]行,");
                    errorMsgSb.append(String.format(CrmErrorInfo.DEPT + "(%s)" + CrmErrorInfo.DEPT_NOT_NULL, columnValue));
                    errorMsgSb.append("\r\n");
                    errorCount++;
                }else {
                    if ( leadsArray[0].getOwnerUserId() == null){
                        leadsArray[0].setDeptId(deptId);
                        leadsArray[0].setDeptName(columnValue);
                    }
                }
            }

            if ("需求描述(*)".equals(o.getValue())){
                if (StringUtils.isBlank(columnValue)){
                    errorMsgSb.append("第[");
                    errorMsgSb.append(rowCount);
                    errorMsgSb.append("]行,");
                    errorMsgSb.append(CrmErrorInfo.REQUIRE_DESCRIPTION_NOT_NULL);
                    errorMsgSb.append("\r\n");
                    errorCount++;
                }else {
                    leadsArray[0].setRequireDescription(columnValue);
                }
            }

            if ("联系电话".equals(o.getValue())){
                if(StringUtils.isNotBlank(columnValue)) {
                    if(phoneSet.contains(columnValue)){
                        errorMsgSb.append("第[");
                        errorMsgSb.append(rowCount);
                        errorMsgSb.append("]行,");
                        errorMsgSb.append("联系电话与Excel中数据重复");
                        errorMsgSb.append("\r\n");
                        errorCount++;
                    }else {
                        phoneSet.add(columnValue);
                        records[0] = Db.findFirst("select accuracy_requirements,customer_level,customer_industry,dept_id,leads_id,batch_id from 72crm_crm_leads where telephone = ?", columnValue);
                    }
                }else{
                    errorMsgSb.append("第[");
                    errorMsgSb.append(rowCount);
                    errorMsgSb.append("]行,");
                    errorMsgSb.append(CrmErrorInfo.TELEPHONE_NOT_NULL);
                    errorMsgSb.append("\r\n");
                    errorCount++;
                }
            }
            if ("公司".equals(o.getValue()) && StringUtils.isNotBlank(columnValue)){
                if(companyNameSet.contains(columnValue)){
                    errorMsgSb.append("第[");
                    errorMsgSb.append(rowCount);
                    errorMsgSb.append("]行,");
                    errorMsgSb.append("公司与Excel中数据重复");
                    errorMsgSb.append("\r\n");
                    errorCount++;
                }else {
                    companyNameSet.add(columnValue);
                    if (records[0] == null){
                        records[0] = Db.findFirst("select accuracy_requirements,customer_level,customer_industry,dept_id,leads_id,batch_id from 72crm_crm_leads where company = ?", columnValue);
                    }
                }
            }

            if("省".equals(o.getValue())){
                addressArray[0] = columnValue;
            }
            if("市".equals(o.getValue())){
                addressArray[1] = columnValue;
            }
            if("区".equals(o.getValue())){
                addressArray[2] = columnValue;
            }

            if ("精度需求".equals(o.getValue()) && StringUtils.isNotBlank(columnValue)){
                leadsArray[0].setAccuracyRequirements(tagMap.getOrDefault(columnValue,""));
                leadsArray[0].setAccuracyRequirementsName(columnValue);
            }
            if ("公司规模".equals(o.getValue()) && StringUtils.isNotBlank(columnValue)){
                leadsArray[0].setCustomerLevel(tagMap.getOrDefault(columnValue,""));
                leadsArray[0].setCustomerLevelName(columnValue);
            }
            if ("行业".equals(o.getValue()) && StringUtils.isNotBlank(columnValue)){
                leadsArray[0].setCustomerIndustry(tagMap.getOrDefault(columnValue,""));
                leadsArray[0].setCustomerIndustryName(columnValue);
            }

            if("公司".equals(o.getValue())){
                leadsArray[0].setCompany(columnValue);
            }
            if("联系电话".equals(o.getValue())){
                leadsArray[0].setTelephone(columnValue);
            }
            if("所在部门".equals(o.getValue())){
                leadsArray[0].setContactDeptName(columnValue);
            }
            if("职位".equals(o.getValue())){
                leadsArray[0].setPosition(columnValue);
            }
            if("邮箱".equals(o.getValue())){
                leadsArray[0].setEmail(columnValue);
            }
            if("微信".equals(o.getValue())){
                leadsArray[0].setWeChat(columnValue);
            }
            if("详细地址".equals(o.getValue())){
                leadsArray[0].setDetailAddress(columnValue);
            }
            if("负责人(邮箱前缀)".equals(o.getValue()) && StringUtils.isNotBlank(columnValue)){
                Integer userId = userMap.get(columnValue);
                if (userId != null){
                    leadsArray[0].setOwnerUserId(userMap.get(columnValue));
                    leadsArray[0].setDeptId(null);
                    leadsArray[0].setDeptName(null);
                } else {
                    errorMsgSb.append("第[");
                    errorMsgSb.append(rowCount);
                    errorMsgSb.append("]行,");
                    errorMsgSb.append("负责人(邮箱前缀) 填写有误");
                    errorMsgSb.append("\r\n");
                    errorCount++;
                }
            }
        });

        addressSb.delete(0,addressSb.length());
        for (String item : addressArray){
            if (StringUtils.isNotBlank(item)){
                if (addressSb.length() > 0){
                    addressSb.append(',');
                }
                addressSb.append(item);
            }
        }
        leadsArray[0].setAddress(addressSb.toString());
        leadsArray[0].setLeadCome(loginDeptId);

        CrmLeads crmLeads = null;
        if (records[0] != null && repeatHandling == 1){
            leadsArray[0].setLeadsId(records[0].getLong("leads_id"));
            leadsArray[0].setBatchId(records[0].getStr("batch_id"));

            crmLeads = new CrmLeads();
            crmLeads.setLeadsId(records[0].getLong("leads_id"));
            crmLeads.setBatchId(records[0].getStr("batch_id"));
            crmLeads.setAccuracyRequirementsName(getKey(tagMap,records[0].getStr("accuracy_requirements")));
            crmLeads.setCustomerLevelName(getKey(tagMap,records[0].getStr("customer_level")));
            crmLeads.setCustomerIndustryName(getKey(tagMap,records[0].getStr("customer_industry")));
            crmLeads.setDeptName(getKey(deptMap,records[0].getStr("dept_id")));

        } else if (records[0] != null && repeatHandling == 2) {
            return;
        }

        dataList.add(new JSONObject().fluentPut("entity",leadsArray[0]).fluentPut("oldEntity",crmLeads));

    }

    public List<String> getHeadList() {
        return headList;
    }

    public List<JSONObject> getDataList() {
        return dataList;
    }
    public void clearDataList() {
        dataList.clear();
    }

    public String getErrMsg(){
        return errorMsgSb.toString();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        companyNameSet.clear();
        phoneSet.clear();
        logger.info("所有数据解析完成！");
    }

    /**
     * 在转换异常 获取其他异常下会调用本接口。抛出异常则停止读取。如果这里不抛出异常则 继续读取下一行。
     *
     * @param context 上下文
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        // 如果是某一个单元格的转换异常 能获取到具体行号
        // 如果要获取头的信息 配合invokeHeadMap使用
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
            logger.error(String.format("第%s行，第%s列解析异常", excelDataConvertException.getRowIndex(),
                    excelDataConvertException.getColumnIndex()));
        }else {
            logger.error(String.format("解析失败:%s", exception.getMessage()));
            throw exception;
        }
    }

    private String getKey(Map<String,String> map, String value){
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                return entry.getKey();
            }
        }
        return null;
    }

}