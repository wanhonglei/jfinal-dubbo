package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.erp.crm.common.CrmDeliveryGoodsCodeExcelEnum;
import com.kakarote.crm9.erp.crm.common.CrmDeliveryInformationExcelEnum;
import com.kakarote.crm9.erp.crm.common.CrmErrorMessage;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmDeliveryInformation;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CrmDeliveryInformationService class
 *
 * @author yue.li
 * @date 2019/11/29
 */
public class CrmDeliveryInformationService {

    private Log logger = Log.getLog(getClass());

    @Inject
    private FieldUtil fieldUtil = new FieldUtil();

    /**
     * 发货信息更新导入excel模板
     * @author yue.li
     */
    public List<Record> queryExcelField(){
        List<Record> fieldList = new LinkedList<>();
        String[] settingArr = new String[]{};
        for(CrmDeliveryInformationExcelEnum crmDeliveryInformationExcelEnum : CrmDeliveryInformationExcelEnum.values()) {
            fieldUtil.getFixedField(fieldList, crmDeliveryInformationExcelEnum.getTypes(), crmDeliveryInformationExcelEnum.getName(), "", "text", settingArr, 0);
        }
        return fieldList;
    }

    /**
     * 导入发货信息
     * @author yue.li
     * @param file 上传文件
     */
    public R uploadExcel(File file) {
        List<CrmDeliveryInformation> deliveryList = new ArrayList<>();
        List<CrmDeliveryInformation> deliveryGoodsList = new ArrayList<>();

        try(InputStream inputStream = Files.newInputStream(file.toPath());
            InputStream inputStream1 = Files.newInputStream(file.toPath());
            ExcelReader reader = ExcelUtil.getReader(inputStream,0,true);
            ExcelReader goodsCodeReader = ExcelUtil.getReader(inputStream1,1,true)) {

            // 读取商品code
            List<List<Object>> excelGoodsCodeInfoList = goodsCodeReader.read();
            if(CollectionUtils.isNotEmpty(excelGoodsCodeInfoList)) {
                List<Object> exportGoodsHeadList = excelGoodsCodeInfoList.get(0);
                Kv kv = new Kv();
                for (int i = 0; i < exportGoodsHeadList.size(); i++) {
                    kv.set(exportGoodsHeadList.get(i), i);
                }
                if (excelGoodsCodeInfoList.size() > 1) {
                    for (int i = 1; i < excelGoodsCodeInfoList.size(); i++) {
                        List<Object> goodsCodeInformationList = excelGoodsCodeInfoList.get(i);
                        CrmDeliveryInformation deliveryGoodsInformation = new CrmDeliveryInformation();
                        Object goodsName = goodsCodeInformationList.get(kv.getInt(CrmDeliveryGoodsCodeExcelEnum.CRM_GOODS_NAME_KEY.getName()));
                        Object goodsSpec = goodsCodeInformationList.get(kv.getInt(CrmDeliveryGoodsCodeExcelEnum.CRM_GOODS_SPEC_KEY.getName()));
                        Object goodsCode = goodsCodeInformationList.get(kv.getInt(CrmDeliveryGoodsCodeExcelEnum.CRM_GOODS_CODE_KEY.getName()));
                        deliveryGoodsInformation.setGoodsName(Objects.isNull(goodsName) ? null: goodsName.toString());
                        deliveryGoodsInformation.setGoodsSpec(Objects.isNull(goodsSpec) ? null: goodsSpec.toString());
                        deliveryGoodsInformation.setGoodsCode(Objects.isNull(goodsCode) ? null: goodsCode.toString());
                        deliveryGoodsList.add(deliveryGoodsInformation);
                    }
                }
            }
            // 读取发货信息
            List<List<Object>> excelInfoList = reader.read();
            if(CollectionUtils.isNotEmpty(excelInfoList)){
                Kv kv = new Kv();
                List<Object> exportHeadList = excelInfoList.get(0);
                for (int i = 0; i < exportHeadList.size(); i++) {
                    kv.set(exportHeadList.get(i), i);
                }
                List<Record> excelFieldList = queryExcelField();
                List<String> templateHeadList = excelFieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
                if (exportHeadList.size() != templateHeadList.size() || !templateHeadList.containsAll(exportHeadList)){
                    return R.error(CrmErrorMessage.NEW_TEMPLATE);
                }
                // 封装excel导入实体
                if (excelInfoList.size() > 1) {
                    for (int i = 1; i < excelInfoList.size(); i++) {
                        List<Object> deliveryInformationList = excelInfoList.get(i);
                        CrmDeliveryInformation deliveryInformation = new CrmDeliveryInformation();
                        Object orderNo = deliveryInformationList.get(kv.getInt(CrmDeliveryInformationExcelEnum.CRM_ORDER_NO_KEY.getName()));
                        Object expressCompany = deliveryInformationList.get(kv.getInt(CrmDeliveryInformationExcelEnum.CRM_EXPRESS_COMPANY_KEY.getName()));
                        Object expressNo = deliveryInformationList.get(kv.getInt(CrmDeliveryInformationExcelEnum.CRM_EXPRESS_NO_KEY.getName()));
                        Object goodsName = deliveryInformationList.get(kv.getInt(CrmDeliveryInformationExcelEnum.CRM_GOODS_NAME_KEY.getName()));
                        Object goodsSpec = deliveryInformationList.get(kv.getInt(CrmDeliveryInformationExcelEnum.CRM_GOODS_SPEC_KEY.getName()));
                        Object hardwareSnNo = deliveryInformationList.get(kv.getInt(CrmDeliveryInformationExcelEnum.CRM_HARDWARE_SN_NO_KEY.getName()));
                        Object num = deliveryInformationList.get(kv.getInt(CrmDeliveryInformationExcelEnum.CRM_NUM_KEY.getName()));
                        deliveryInformation.setDeliveryId(IdUtil.simpleUUID());
                        deliveryInformation.setOrderNo(Objects.isNull(orderNo) ? null: orderNo.toString().trim());
                        deliveryInformation.setExpressCompany(Objects.isNull(expressCompany) ? null:expressCompany.toString());
                        deliveryInformation.setExpressNo(Objects.isNull(expressNo) ? null: expressNo.toString());
                        deliveryInformation.setGoodsName(Objects.isNull(goodsName) ? null: goodsName.toString());
                        deliveryInformation.setGoodsSpec(Objects.isNull(goodsSpec) ? null: goodsSpec.toString());
                        deliveryInformation.setHardwareSnNo(Objects.isNull(hardwareSnNo) ? null: hardwareSnNo.toString());
                        deliveryInformation.setNum(Objects.isNull(num) ? null: StringUtils.isNotEmpty(num.toString()) ? Integer.valueOf(num.toString()) : null);
                        deliveryInformation.setOperatorId(Objects.isNull(BaseUtil.getUserId()) ? null : BaseUtil.getUserId().intValue());
                        deliveryInformation.setGoodsCode(getGoodsCode(deliveryGoodsList,deliveryInformation.getGoodsName(),deliveryInformation.getGoodsSpec()));
                        deliveryList.add(deliveryInformation);
                    }
                    R result = checkDeliveryInformation(deliveryList);
                    if(result.isSuccess()){
                        dealDeliveryList(deliveryList);
                    }else{
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("uploadExcel deliveryInformation msg:%s", BaseUtil.getExceptionStack(e)));
            return R.error();
        } finally {
            if(file.exists()) {
                if(file.delete()) {
                    logger.info("delete upload file success");
                }else {
                    logger.info(String.format("delete upload file fail path:%s", file.getAbsolutePath()));
                }
            }
        }
        return R.ok().put("data",deliveryList);
    }

    /**
     * 校验发货数据合法性
     * @author yue.li
     * @param deliveryList 发货数据集合
     */
    private R checkDeliveryInformation(List<CrmDeliveryInformation> deliveryList) {
        for(CrmDeliveryInformation deliveryInformation : deliveryList) {
            if(StringUtils.isEmpty(deliveryInformation.getOrderNo())) {
                return R.error(CrmErrorMessage.ORDER_NO_IS_NULL);
            }
            if(StringUtils.isNotEmpty(deliveryInformation.getOrderNo()) && !deliveryInformation.getOrderNo().startsWith(CrmConstant.PREFIX_ORDER)){
                return R.error(CrmErrorMessage.ORDER_IS_NOT_LEGAL + deliveryInformation.getOrderNo());
            }
            if(StringUtils.countMatches(deliveryInformation.getOrderNo(),CrmConstant.PREFIX_ORDER) > 1) {
                return R.error(CrmErrorMessage.ORDER_IS_NOT_LEGAL + deliveryInformation.getOrderNo());
            }
            if(StringUtils.isEmpty(deliveryInformation.getExpressCompany())) {
                return R.error(CrmErrorMessage.EXPRESS_COMPANY_IS_NULL);
            }
            if(StringUtils.isEmpty(deliveryInformation.getExpressNo())) {
                return R.error(CrmErrorMessage.EXPRESS_NO_IS_NULL);
            }
            if(StringUtils.isEmpty(deliveryInformation.getGoodsName())) {
                return R.error(CrmErrorMessage.GOODS_NAME_IS_NULL);
            }
            if(StringUtils.isEmpty(deliveryInformation.getGoodsSpec())) {
                return R.error(CrmErrorMessage.GOODS_SPEC_IS_NULL);
            }
            if(Objects.isNull(deliveryInformation.getNum())) {
                return R.error(CrmErrorMessage.NUM_IS_NULL);
            }
        }
        return R.ok();
    }

    /**
     * 处理发货数据集合
     * @author yue.li
     * @param deliveryList 发货实体集合
     */
    @Before(Tx.class)
    private void dealDeliveryList(List<CrmDeliveryInformation> deliveryList) {
        List<CrmDeliveryInformation> addDeliveryList = new ArrayList<>();
        List<CrmDeliveryInformation> updateDeliveryList = new ArrayList<>();
        for(CrmDeliveryInformation crmDeliveryInformation : deliveryList) {
            List<Record> recordList = Db.find(Db.getSql("crm.delivery.getDeliveryInfo"),crmDeliveryInformation.getOrderNo(),crmDeliveryInformation.getExpressNo(),crmDeliveryInformation.getGoodsName(),crmDeliveryInformation.getGoodsSpec());
            List<CrmDeliveryInformation> updateInfoList = recordList.stream().map(item->new CrmDeliveryInformation()._setAttrs(item.getColumns())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(updateInfoList)){
                crmDeliveryInformation.setDeliveryId(updateInfoList.get(0).getDeliveryId());
                crmDeliveryInformation.setId(updateInfoList.get(0).getId());
                updateDeliveryList.add(crmDeliveryInformation);
            }else{
                addDeliveryList.add(crmDeliveryInformation);
            }
        }

        // 批量更新发货数据
        List<Record> updateRecords = updateDeliveryList.stream().map(Model::toRecord).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(updateRecords)){
            Db.batchUpdate("72crm_crm_delivery_information", updateRecords, updateRecords.size());
        }

        // 批量保存发货数据
        List<Record> addRecords = addDeliveryList.stream().map(Model::toRecord).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(addRecords)){
            Db.batchSave("72crm_crm_delivery_information", addRecords, addRecords.size());
        }
    }

    /**
     * 获取goodsCode
     * @author yue.li
     * @param deliveryGoodsList 发货商品集合
     * @param goodsName 商品名称
     * @param goodsSpec 商品规格
     */
    private String getGoodsCode(List<CrmDeliveryInformation> deliveryGoodsList,String goodsName,String goodsSpec) {
        String goodsCode = null;
        for(CrmDeliveryInformation crmDeliveryInformation : deliveryGoodsList) {
            if(StringUtils.isNotEmpty(crmDeliveryInformation.getGoodsName()) && crmDeliveryInformation.getGoodsName().equals(goodsName)
               && StringUtils.isNotEmpty(crmDeliveryInformation.getGoodsSpec()) && crmDeliveryInformation.getGoodsSpec().equals(goodsSpec)) {
                goodsCode = crmDeliveryInformation.getGoodsCode();
                break;
            }
        }
        return goodsCode;
    }
}
