package com.kakarote.crm9.erp.crm.entity;

import com.jfinal.kit.JsonKit;
import lombok.Data;

/**
 * Sign In Item In List Page
 *
 * @author hao.fu
 * @since 2019/12/13 15:47
 */
@Data
public class SignInItem {

    private String signinHistoryId;
    private String addressId;
    private String signinTime;
    private String signinUserId;
    private String noteId;
    private String notes;
    private String customerId;
    private String province;
    private String city;
    private String district;
    private String location;
    private String signer;
    private String clientName;
    private String customerAddress;
    private String siteMemberId;
    private boolean isLocationMatch;

    private String customerType;
    private String customerGrade;
    private String customerGradeDesc;
    private String customerTypeDesc;

    @Override
    public String toString() {
        return JsonKit.toJson(this);
    }
}
