package com.kakarote.crm9.erp.crm.service;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.crm.entity.CrmBatchRecord;

import java.util.Calendar;
public class CrmBatchRecordService {

    /**
     * @author liyue
     * 获取编号信息
     */
    public String getBatchNo(String type){
        int index = 0;
        String nowTime = getYearMonthDate();
        CrmBatchRecord batchRecord = new CrmBatchRecord();
        Record record = Db.findFirst("select batch_no as batchNo from 72crm_crm_batch_record where type = ? and batch_date = ?",type,nowTime);
        if (null == record){
            batchRecord.setBatchNo(index+1);
            batchRecord.setType(type);
            batchRecord.setBatchDate(nowTime);
            batchRecord.setBatchId(null);
            batchRecord.save();
        }else{
            batchRecord.setBatchNo(record.getInt("batchNo") + 1);
            Db.update("update 72crm_crm_batch_record set batch_no = ? where type= ? and batch_date = ? ",batchRecord.getBatchNo(),type,nowTime);
        }
        return nowTime + String.format("%0" + 4 + "d", batchRecord.getBatchNo());
    }

    public String getYearMonthDate(){
        String resultYear = "";
        String resultMonth = "";
        String resultDay = "";
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        resultYear = String.valueOf(year);
        if(month<10){
            resultMonth = "0"+ month;
        }else{
            resultMonth = String.valueOf(month);
        }
        int day = c.get(Calendar.DAY_OF_MONTH);
        if(day<10){
            resultDay = "0" + day;
        }else{
            resultDay = String.valueOf(day);
        }
        return resultYear + resultMonth + resultDay;
    }
}
