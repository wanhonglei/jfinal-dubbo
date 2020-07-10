package com.kakarote.crm9.erp.crm.common;

public enum CrmRegionEnum {
    /**
     * CrmRegionEnum
     */
    BJ_KEY("北京", "bj"),
    SH_KEY("上海", "sh"),
    CQ_KEY("重庆","cq"),
    XG_KEY("香港","xg"),
    AM_KEY("澳门","am"),
    SJZ_KEY("石家庄","sjz"),
    TY_KEY("太原","ty"),
    HT_KEY("呼和浩特","ht"),
    SY_KEY("沈阳","sy"),
    CC_KEY("长春","cc"),
    HEB_KEY("哈尔滨","heb"),
    NJ_KEY("南京","nj"),
    HZ_KEY("杭州","hz"),
    HF_KEY("合肥","hf"),
    FZ_KEY("福州","fz"),
    NC_KEY("南昌","nc"),
    JN_KEY("济南","jn"),
    ZZ_KEY("郑州","zz"),
    WH_KEY("武汉","wh"),
    CS_KEY("长沙","cs"),
    GZ_KEY("广州","gz"),
    NN_KEY("南宁","nn"),
    HK_KEY("海口","hk"),
    CD_KEY("成都","cd"),
    GY_KEY("贵阳","gy"),
    KM_KEY("昆明","km"),
    LS_KEY("拉萨","ls"),
    XA_KEY("西安","xa"),
    LZ_KEY("兰州","lz"),
    XN_KEY("西宁","xn"),
    YC_KEY("银川","yc"),
    WL_KEY("乌鲁木齐","wl"),
    TB_KEY("台北","tb");

    private String name;
    private String types;
    CrmRegionEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmRegionEnum c : CrmRegionEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
            }
        }
        return "";
    }
    public String getName() {
        return name;
    }

    public String getTypes() {
        return types;
    }

    public static String getTypeByName(String name) {
        CrmRegionEnum[] items = CrmRegionEnum.values();
        for(CrmRegionEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
