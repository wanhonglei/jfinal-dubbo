package com.kakarote.crm9.erp.crm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Location Info
 *
 * @author hao.fu
 * @since 2019/12/26 13:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationInfo {

    private String poiName;
    private String typeCode;
    private String location;
    private String customerId;
    private String customerName;
    private String customerAddress;
    private String customerPCD;
    private String signInTime;

    public LocationInfo(String signInTime) {
        this.signInTime = signInTime;
    }
}
