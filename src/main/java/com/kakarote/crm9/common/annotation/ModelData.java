package com.kakarote.crm9.common.annotation;

import java.lang.annotation.*;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:hao.fu Created by hao.fu on 2019/12/3.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Inherited
@Documented
public @interface ModelData {
}
