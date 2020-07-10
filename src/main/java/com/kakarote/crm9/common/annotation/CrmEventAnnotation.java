package com.kakarote.crm9.common.annotation;

import com.kakarote.crm9.erp.crm.common.CrmEventEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: honglei.wan
 * @Description: crm事件注解，用于更新业务关联表冗余状态等数据
 * @Date: Create in 2020/5/15 1:40 下午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
@Inherited
@Documented
public @interface CrmEventAnnotation {
	/**
	 * 获取事件枚举
	 * @return
	 */
	CrmEventEnum crmEventEnum();

}
