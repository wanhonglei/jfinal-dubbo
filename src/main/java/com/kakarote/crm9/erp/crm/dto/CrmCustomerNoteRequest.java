package com.kakarote.crm9.erp.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CrmCustomerNoteRequest class
 *
 * @author yue.li
 * @date 2020/04/07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerNoteRequest {

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 小记内容
     */
    private String noteContent;
}
