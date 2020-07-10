package com.kakarote.crm9.mobile.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Mobile Contacts List Request
 *
 * @author hao.fu
 * @since 2020/1/15 9:40
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MobileContactsListRequest extends AbstractPageListRequest {

    /**
     * 搜索条件：联系人名称/手机号码
     */
    private String searchKey;

    /**
     * 联系人角色
     */
    private String contactsRole;

}
