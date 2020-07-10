package com.kakarote.crm9.utils.common;

import java.util.Collection;
import java.util.List;

import org.dozer.DozerBeanMapper;

import com.google.common.collect.Lists;

/**
 * 封装并持有Dozer单例，深度转换Bean<->Bean的Mapper，实现： 1. 转换对象的类型 2. 转换Collection中对象的类型 3. 将对象A的值拷贝到对象B中
 * @author xiaowen.wu
 *
 */
public class BeanMapper {

	   /**
     * 持有Dozer单例
     */
    private static DozerBeanMapper dozer = new DozerBeanMapper();

    /**
     * 转换对象的类型
     */
    public static <T> T map(Object source, Class<T> destinationClass) {
        return dozer.map(source, destinationClass);
    }

    /**
     * 转换Collection中对象的类型
     */
    @SuppressWarnings("rawtypes")
    public static <T> List<T> mapList(Collection sourceList, Class<T> destinationClass) {
        List<T> destinationList = Lists.newArrayList();
        for (Object sourceObject : sourceList) {
            T destinationObject = dozer.map(sourceObject, destinationClass);
            destinationList.add(destinationObject);
        }
        return destinationList;
    }

    /**
     * 将对象A的值拷贝到对象B中
     */
    public static <T> T copy(Object source, T destinationObject) {
        dozer.map(source, destinationObject);
        return destinationObject;
    }
}
