package com.kakarote.crm9.erp.crm.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BOPS订单渠道
 * @Author: haihong.wu
 * @Date: 2020/3/17 6:56 下午
 */
public enum BopsOrderChannelEnum {
    PC(1L, "pc", true, "PC端下单"),
    WEIXINH5(2L, "h5", true, "微信H5下单"),
    IOS(3L, "iOS", true, "ios下单"),
    OA(4L, "oa", true, "oa下单"),
    ANDROID(5L, "android", true, "安卓下单"),
    IOS_ONE_KEY(6L, "iOS-oneKey", true, "ios一键下单"),
    ANDROID_ONE_KEY(7L, "android-oneKey", true, "安卓一键下单"),
    DISTRIBUTOR(8L, "DR", false, "分销商"),
    GLOBAL(9L, "global", false, "国际站"),
    WX_MP(10L, "wx_mp", true, "微信小程序"),
    ;

    public static List<Long> listOnlineBusinessCodes() {
       return Arrays.stream(values())
               .filter(BopsOrderChannelEnum::isBelongToOnlineBusiness)
               .map(BopsOrderChannelEnum::getCode)
               .collect(Collectors.toList());
    }

    public static List<String> listOnlineBusinessKeys() {
        return Arrays.stream(values())
                .filter(BopsOrderChannelEnum::isBelongToOnlineBusiness)
                .map(BopsOrderChannelEnum::getKey)
                .collect(Collectors.toList());
    }

    public static BopsOrderChannelEnum findByCode(Long code) {
        for (BopsOrderChannelEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    private Long code;
    private String key;
    private boolean belongToOnlineBusiness;
    private String desc;

    BopsOrderChannelEnum(Long code, String key, boolean belongToOnlineBusiness, String desc) {
        this.code = code;
        this.key = key;
        this.belongToOnlineBusiness = belongToOnlineBusiness;
        this.desc = desc;
    }

    public Long getCode() {
        return code;
    }

    public String getKey() {
        return key;
    }

    public boolean isBelongToOnlineBusiness() {
        return belongToOnlineBusiness;
    }

    public String getDesc() {
        return desc;
    }
}

