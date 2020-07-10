package com.kakarote.crm9.mobile.entity;

import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mobile New Sign In Request
 *
 * @author hao.fu
 * @since 2019/12/31 16:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileNewSignInRequest {
    private String longitude;
    private String latitude;
    private String address;
    private String typeCode;
    private String poiName;
    private String province;
    private String city;
    private String district;
    private Integer customerId;
    private boolean isUpdate;
    private String signInTime;
    private AdminRecord noteEntity;
}
