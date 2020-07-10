package com.kakarote.crm9.mobile.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Mobile Sign In List Request
 *
 * @author hao.fu
 * @since 2020/1/13 19:56
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MobileSignInListRequest extends AbstractPageListRequest {

    private String customerName;
}
