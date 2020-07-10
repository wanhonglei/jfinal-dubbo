package com.kakarote.crm9.erp.crm.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmExcelConstant;
import com.kakarote.crm9.erp.crm.service.CrmDeliveryInformationService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

/**
 * CrmDeliveryInformationController class
 *
 * @author yue.li
 * @date 2019/11/29
 */
@Before(IocInterceptor.class)
public class CrmDeliveryInformationController extends Controller {

    @Inject
    private CrmDeliveryInformationService crmDeliveryInformationService;

    private Log logger = Log.getLog(getClass());

    /**
     * 发货信息获取导入模板
     * @author yue.li
     */
    @Permissions("crm:delivery:downloadExcel")
    public void downloadExcel(){
        try(HSSFWorkbook wb = new HSSFWorkbook()){
            List<Record> recordList = crmDeliveryInformationService.queryExcelField();
            HSSFSheet sheet = wb.createSheet(CrmExcelConstant.DELIVERY_INFORMATION_DOWNLOAD_NAME);
            HSSFRow row = sheet.createRow(0);
            for (int i=0;i < recordList.size();i++){
                Record record = recordList.get(i);
                HSSFCell cell = row.createCell(i);
                cell.setCellValue(record.getStr("name"));
            }
            HttpServletResponse response = getResponse();
            response.setContentType(CrmConstant.CONTENT_TYPE);
            response.setCharacterEncoding(CrmConstant.UTF);
            response.setHeader("Content-Disposition", "attachment;filename= "+CrmExcelConstant.DELIVERY_INFORMATION_DOWNLOAD_NAME+".xls");
            wb.write(response.getOutputStream());
            wb.close();
            renderNull();
        } catch (Exception e){
            logger.error(String.format("downloadExcel deliveryInformation msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 发货信息导入
     * @author yue.li
     */
    @Permissions("crm:delivery:uploadExcel")
    public void uploadExcel(@Para("file") File file){
        try{
            renderJson(crmDeliveryInformationService.uploadExcel(file));
        } catch (Exception e){
            logger.error(String.format("uploadExcel deliveryInformation msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }
}
