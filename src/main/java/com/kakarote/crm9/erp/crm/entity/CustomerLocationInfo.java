package com.kakarote.crm9.erp.crm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer Location Info
 *
 * @author hao.fu
 * @since 2020/1/10 16:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLocationInfo {

    String customerId;
    String customerName;
    String customerAddress;
    String customerPCD;
    String location;
    String longitude;
    String latitude;
    String signInTime;
    String addressId;
}
