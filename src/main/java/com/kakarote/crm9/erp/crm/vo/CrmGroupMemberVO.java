package com.kakarote.crm9.erp.crm.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 根据以前的接口定义写的VO，拒绝吐槽
 * @Author: haihong.wu
 * @Date: 2020/5/15 3:16 下午
 */
@Data
public class CrmGroupMemberVO implements Serializable {
    private static final long serialVersionUID = -7486548487800621637L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 职位
     */
    private String name;

    /**
     * 姓名
     */
    private String realname;

    /**
     * 权限
     */
    private String power;

    /**
     * 团队角色
     */
    private String groupRole;
}
