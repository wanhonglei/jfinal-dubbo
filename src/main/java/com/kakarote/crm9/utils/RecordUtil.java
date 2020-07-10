package com.kakarote.crm9.utils;

import cn.hutool.core.bean.BeanUtil;
import com.jfinal.plugin.activerecord.Record;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/19 4:56 下午
 */
@Slf4j
public class RecordUtil {

    /**
     * 将Record转为Bean
     *
     * @param record
     * @param clz
     * @param <T>
     * @return
     */
    public static <T> T toBean(Record record, Class<T> clz) {
        if (record == null || clz == null) {
            return null;
        }
        if (clz.equals(Record.class) && clz.getClassLoader().equals(record.getClass().getClassLoader())) {
            return (T) record;
        }
        try {
            T instance = clz.newInstance();
            PropertyDescriptor[] propertyDescriptors = BeanUtil.getPropertyDescriptors(clz);
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String fieldName = propertyDescriptor.getDisplayName();
                Method writeMethod = propertyDescriptor.getWriteMethod();
                if (writeMethod == null) {
                    continue;
                }
                Object value = record.get(fieldName);
                boolean access = writeMethod.isAccessible();
                writeMethod.setAccessible(true);
                try {
                    writeMethod.invoke(instance, value);
                } catch (InvocationTargetException e) {
                    log.warn("RecordUtil to bean error[field:{},errorMsg:{}]", fieldName, e.getMessage());
                }
                writeMethod.setAccessible(access);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("--RecordUtil toBean error--", e);
        }
        return null;
    }

    public static <T> List<T> toBeanList(List<Record> recordList, Class<T> clz) {
        if (recordList == null || clz == null) {
            return null;
        }
        return recordList.stream().map(record -> toBean(record, clz)).collect(Collectors.toList());
    }

    /**
     * 将Bean转为Record
     *
     * @param obj
     * @return
     */
    public static Record toRecord(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Record) {
            return (Record) obj;
        }
        try {
            Record record = new Record();
            Class<?> clz = obj.getClass();
            if (!clz.equals(Object.class)) {
                PropertyDescriptor[] propertyDescriptors = BeanUtil.getPropertyDescriptors(clz);
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    String fieldName = propertyDescriptor.getDisplayName();
                    Method readMethod = propertyDescriptor.getReadMethod();
                    if (readMethod == null) {
                        continue;
                    }
                    boolean access = readMethod.isAccessible();
                    readMethod.setAccessible(true);
                    Object value = null;
                    try {
                        value = readMethod.invoke(obj);
                    } catch (InvocationTargetException e) {
                        log.warn("RecordUtil to record error[field:{},errorMsg:{}]", fieldName, e.getMessage());
                    }
                    record.set(fieldName, value);
                    readMethod.setAccessible(access);
                }
            }
            return record;
        } catch (IllegalAccessException e) {
            log.error("--RecordUtil toRecord error--", e);
        }
        return null;
    }

    public static List<Record> toRecordList(List<?> objList) {
        if (objList == null) {
            return null;
        }
        return objList.stream().map(obj -> toRecord(obj)).collect(Collectors.toList());
    }
}
