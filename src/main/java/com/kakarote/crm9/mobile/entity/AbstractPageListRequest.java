package com.kakarote.crm9.mobile.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Abstract Page List Request
 *
 * @author hao.fu
 * @since 2020/1/17 16:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbstractPageListRequest {

    /**
     * 业务id
     */
    private Integer bizType;

    /**
     * 场景id
     */
    private Integer sceneId;

    /**
     * 创建时间 0:最近一周 1:最近两周 2:最近一个月
     */
    private String createTimeType;

    /**
     * 客户类型
     */
    private Integer customerType;

    /**
     * 客户等级
     */
    private Integer customerGrade;

    /**
     * 域账号, 多个用逗号分割
     */
    private String ownerUserLoginIds;

}
