package com.kakarote.crm9.erp.crm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.crm.common.CrmBusinessInformationExcelEnum;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessExcel;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CrmBusinessDataListener
 *
 * @author yue.li
 */
public class CrmBusinessDataListener extends AnalysisEventListener<Map<Integer, String>> {
    private static final Log logger = Log.getLog(CrmBusinessDataListener.class);

    private static List<String> headList = new ArrayList<>(16);

    private AdminDeptService adminDeptService;

    private List<CrmBusinessExcel> dataList = new ArrayList<>(5000);
    private Set<Map.Entry<Integer, String>> entrySet = null;
    private int rowCount = 1;
    private StringBuilder errorMsgSb = new StringBuilder();
    private static Map<String,Integer> userMap;
    private static Map<Integer,Integer> userDeptMap;

    public CrmBusinessDataListener(AdminDeptService adminDeptService){
        this.adminDeptService = adminDeptService;
    }

    static {
        headList.add(CrmBusinessInformationExcelEnum.CRM_BUSINESS_NAME_KEY.getName());
        headList.add(CrmBusinessInformationExcelEnum.CRM_CUSTOMER_NAME_KEY.getName());
        headList.add(CrmBusinessInformationExcelEnum.CRM_CREATE_TIME_KEY.getName());
        headList.add(CrmBusinessInformationExcelEnum.CRM_UPDATE_TIME_KEY.getName());
        headList.add(CrmBusinessInformationExcelEnum.CRM_OWNER_USER_NAME_KEY.getName());
        headList.add(CrmBusinessInformationExcelEnum.CRM_CREATE_USER_NAME_KEY.getName());

        //加载人员树
        userMap = Db.find(Db.getSql("admin.user.getAllUsers")).stream().collect(Collectors.toMap(o1 -> o1.getStr("realName"), o1 -> o1.getInt("userId"), (key1, key2)->key1));
        //加载人员部门
        userDeptMap = Db.find(Db.getSql("admin.user.getAllUsersDept")).stream().collect(Collectors.toMap(o1 -> o1.getInt("userId"), o1 -> o1.getInt("deptId"), (key1, key2)->key1));
    }

    /**
     * 这里会一行行的返回头
     * @param headMap 头
     * @param context 上下文
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        if(CollectionUtils.isEmpty(headList)) {
            return;
        }
        headMap.entrySet().iterator().forEachRemaining(o -> headList.remove(o.getValue()));

        if(headList.size() > 0){
            return;
        }
        //头对应列名和列index
        entrySet = headMap.entrySet();

    }

    @Override
    public void invoke(Map<Integer, String> map, AnalysisContext context) {

        CrmBusinessExcel crmBusinessExcel = new CrmBusinessExcel();

        rowCount++;
        entrySet.forEach(o -> {
            String columnValue = map.get(o.getKey());
            if (CrmBusinessInformationExcelEnum.CRM_BUSINESS_NAME_KEY.getName().equals(o.getValue())){
                if (StringUtils.isEmpty(columnValue)){
                    errorMsgSb.append("第[");
                    errorMsgSb.append(rowCount);
                    errorMsgSb.append("]行,");
                    errorMsgSb.append("商机名称不能为空");
                    errorMsgSb.append("\r\n");
                }else {
                    crmBusinessExcel.setBusinessName(columnValue);
                }
            }

            if (CrmBusinessInformationExcelEnum.CRM_CUSTOMER_NAME_KEY.getName().equals(o.getValue())){
                if (StringUtils.isEmpty(columnValue)){
                    errorMsgSb.append("第[");
                    errorMsgSb.append(rowCount);
                    errorMsgSb.append("]行,");
                    errorMsgSb.append("客户名称不能为空");
                    errorMsgSb.append("\r\n");
                }else {
                    CrmCustomer crmCustomer = CrmCustomer.dao.findFirst(Db.getSql("crm.customer.getCustomerIdByCustomerName"),columnValue);
                    crmBusinessExcel.setCustomerId(Objects.nonNull(crmCustomer) ? crmCustomer.getCustomerId() : null);
                    crmBusinessExcel.setCustomerName(columnValue);
                }
            }

            if(CrmBusinessInformationExcelEnum.CRM_OWNER_USER_NAME_KEY.getName().equals(o.getValue()) && StringUtils.isNotBlank(columnValue)){
                Integer userId = userMap.get(columnValue);
                if (Objects.isNull(userId)){
                    errorMsgSb.append("第[");
                    errorMsgSb.append(rowCount);
                    errorMsgSb.append("]行,");
                    errorMsgSb.append("负责人名称在CRM找不到");
                    errorMsgSb.append("\r\n");
                }else {
                    Integer deptId = userDeptMap.get(userId);
                    String businessDepartmentId = adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(deptId));
                    crmBusinessExcel.setDeptId(StringUtils.isNotEmpty(businessDepartmentId) ? Integer.valueOf(businessDepartmentId) : null);
                    crmBusinessExcel.setOwnerUserId(userId);
                    crmBusinessExcel.setOwnerUserName(columnValue);
                    crmBusinessExcel.setCreateUserId(userId);
                    crmBusinessExcel.setCreateUserName(columnValue);
                }
            }

            if (CrmBusinessInformationExcelEnum.CRM_CREATE_TIME_KEY.getName().equals(o.getValue())){
                crmBusinessExcel.setCreateTime(columnValue);
            }
            if (CrmBusinessInformationExcelEnum.CRM_UPDATE_TIME_KEY.getName().equals(o.getValue())){
                crmBusinessExcel.setUpdateTime(columnValue);
            }
        });
        dataList.add(crmBusinessExcel);

    }

    public List<CrmBusinessExcel> getDataList() {
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

}