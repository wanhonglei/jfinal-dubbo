package com.kakarote.crm9.utils;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Excel Export Util
 *
 * @author yue.li
 */
public class ExcelExportUtil {

    private static Pattern DOUBLE_PATTERN = Pattern.compile("^[-|+]?\\d+\\.\\d+$");
    private static Pattern INTEGER_PATTERN = Pattern.compile("^[-|+]?\\d+$");

    /***
     * 通用导出多个sheet
     * @author yue.li
     * @param headList 表头Map集合
     * @param recordList 导出集合
     * @param fileName 文件名
     * @param mergeList merge集合
     */
    public static void export(List<LinkedHashMap<String,String>> headList, List<List<Record>> recordList, String fileName,HttpServletResponse httpServletResponse,List<String> mergeList) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter();
        if(headList != null && headList.size() >0) {
            for(int i=0;i<headList.size();i++){
                if(mergeList != null && mergeList.size() >0 ) {
                    CellRangeAddress address = new CellRangeAddress(0,0,0,headList.get(i).size()-1);
                    writer.getSheets().get(i).addMergedRegion(address);
                    Cell cell = writer.getOrCreateCell(0,0);
                    cell.setCellValue(mergeList.get(i));
                    writer.setCurrentRow(1);
                }
                Iterator<String> iterator =  headList.get(i).keySet().iterator();
                Map<String, String> headerAlias = new LinkedHashMap<>();
                while (iterator.hasNext()) {
                    for (Map.Entry<String, String> entry : headList.get(i).entrySet()) {
                        if(entry.getKey().equals(iterator.next())){
                            headerAlias.put(entry.getKey(),entry.getValue());
                        }
                    }
                }
                writer.setHeaderAlias(headerAlias);
                List<Map<String,Object>> list = new ArrayList<>();
                List<Record> dataResult = recordList.get(i);

                for (Record record : dataResult){
                    Record recordAdd = new Record();
                    for (Map.Entry<String, String> entry : headList.get(i).entrySet()) {
                        String value = record.getStr(entry.getKey());
                        if(StringUtils.isNotEmpty(value) && isNumeric(value)) {
                            if(value.length() > 9) {
                                recordAdd.set(entry.getKey(),record.getStr(entry.getKey()));
                            }else{
                                recordAdd.set(entry.getKey(),Integer.valueOf(value));
                            }
                        }else if(StringUtils.isNotEmpty(value) && isDouble(value)) {
                            recordAdd.set(entry.getKey(), Double.valueOf(value));
                        }else{
                            recordAdd.set(entry.getKey(),record.getStr(entry.getKey()));
                        }
                    }
                    list.add(recordAdd.getColumns());
                }
                for(int j =0;j<headList.get(i).size();j++){
                    writer.setColumnWidth(j,20);
                }
                writer.write(list,true);
                if(headList.size() >1){
                    writer.setSheet("sheet" + i);
                }
                writer.renameSheet(i,"sheet" + (i +1));
            }

            CellStyle cellStyle = writer.getCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
        }
        //自定义标题别名
        httpServletResponse.setContentType(CrmConstant.CONTENT_TYPE);
        httpServletResponse.setCharacterEncoding(CrmConstant.UTF);
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename= "+fileName+ ".xls");
        ServletOutputStream out = httpServletResponse.getOutputStream();
        writer.flush(out);
        // 关闭writer，释放内存
        writer.close();
    }

    /***
     * 判断是否为double类型
     * @author yue.li
     * @param str 字符串
     * @return true if the string is double
     */
    private static boolean isDouble(String str) {
        boolean flag = true;
        try{
            Double.valueOf(str);
        }catch(Exception e) {
            flag = false;
        }
        return flag;
    }

    /***
     * 判断是否为整形
     * @author yue.li
     * @param str 字符串
     * @return true if the string is integer
     */
    private static boolean isNumeric(String str) {
        boolean flag = true;
        try{
            Integer.valueOf(str);
        }catch(Exception e) {
            flag = false;
        }
        return flag;
    }
}
