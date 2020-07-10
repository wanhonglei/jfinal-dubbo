package com.kakarote.crm9.utils;

import com.jfinal.log.Log;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
/**
 * CrmDateUtil class
 *
 * @author yue.li
 * @date 2020/01/08
 */
public class CrmDateUtil {

    private Log logger = Log.getLog(getClass());
    /***
     * 获取一周前时间
     * @author yue.li
     * @return
     */
    public static String getLastWeek(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, - 7);
        return sdf.format(cal.getTime());
    }

    /***
     * 获取传入日期所在月的第一天
     * @author yue.li
     * @return
     */
    public static Date getFirstDayDateOfMonth(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int last = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, last);
        return cal.getTime();
    }

    /***
     * 获取传入日期所在月的最后一天
     * @author yue.li
     * @return
     */
    public static Date getLastDayOfMonth(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int last = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, last);
        return cal.getTime();
    }

    public static String formatDate(String date) {
        String result = null;
        if (StringUtils.isNotEmpty(date)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                result = sdf.format(sdf.parse(date));
            } catch (Exception e) {
                new CrmDateUtil().logger.error(e.getMessage());
            }
        }
        return result;
    }

    public static String formatDate(Date date) {
        String result = null;
        if (Objects.nonNull(date)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                result = sdf.format(date);
            } catch (Exception e) {
                new CrmDateUtil().logger.error(e.getMessage());
            }
        }
        return result;
    }

    /**
     * 格式化日期为年月日小时分秒
     * @author yue.li
     * @param date 日期
     */
    public static String formatDateHours(Date date) {
        String result = null;
        if (Objects.nonNull(date)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                result = sdf.format(date);
            } catch (Exception e) {
                new CrmDateUtil().logger.error(e.getMessage());
            }
        }
        return result;
    }

    /**
     * 将日期字符串还原成日期类型
     * @author yue.li
     * @param date 日期字符串
     */
    public static Date parseDateHours(String date) {
        Date result = null;
        if (Objects.nonNull(date)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                result = sdf.parse(date);
            } catch (Exception e) {
                new CrmDateUtil().logger.error(e.getMessage());
            }
        }
        return result;
    }

    /***
     * 获取两周前时间
     * @author yue.li
     * @return
     */
    public static String getLastTwoWeek(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, - 14);
        return sdf.format(cal.getTime());
    }

    /***
     * 获取一个月前时间
     * @author yue.li
     * @return
     */
    public static String getLastOneMonth(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, - 1);
        return sdf.format(cal.getTime());
    }

    /***
     * 获取几天之前时间
     * @author yue.li
     * @param day 日期天数
     * @return
     */
    public static String getLastParamDay(int day){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0- day);
        return sdf.format(cal.getTime());
    }
}
