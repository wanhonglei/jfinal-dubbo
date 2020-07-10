package com.kakarote.crm9.erp.crm.dto;

import com.jfinal.kit.JsonKit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crm Signin Page Request
 *
 * @author hao.fu
 * @since 2019/11/19 19:34
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmSignInPageRequest {

    private String keyword;

    private String startTime;

    private String endTime;

    private String location;

    private String province;

    private String city;

    private String district;

    private Boolean haveNotes;

    /**
     * 业务id
     */
    private int bizType;

    /**
     * 场景id
     */
    private int sceneId;

    /**
     * 签到人
     */
    private Integer signer;

    @Override
    public String toString() {
        return JsonKit.toJson(this);
    }
}
