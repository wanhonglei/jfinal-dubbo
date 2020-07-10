package com.kakarote.crm9.erp.crm.entity;

import com.jfinal.kit.JsonKit;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Crm Scene POJO
 *
 * @author hao.fu
 * @since 2019/12/30 15:36
 */
@Data
@NoArgsConstructor
public class CrmScene implements Serializable {
    private static final long serialVersionUID = 6378843832850331643L;

    private int sceneId;
    private String sceneName;
    private boolean isDefault;
    private boolean isPc;
    private boolean isMobile;

    public CrmScene(int id, String name, boolean isDefault,boolean isPc,boolean isMobile) {
        sceneId = id;
        sceneName = name;
        this.isDefault = isDefault;
        this.isPc = isPc;
        this.isMobile = isMobile;
    }

    @Override
    public String toString() {
        return JsonKit.toJson(this);
    }
}
