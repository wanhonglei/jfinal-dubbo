package com.kakarote.crm9.integration.common;

/**
 * @author xiaowen
 */
public enum DistributorSaleAreaEnum {
	/**
	 * DistributorSaleAreaEnum
	 */
	BEI_JING_SHI("110000","北京市"),
	TIAN_JIN_SHI("120000","天津市"),
	HE_BEI_SHENG("130000","河北省"),
	SHAN_XI_SHENG_JIN("140000","山西省"),
	NEI_MENG_GU_ZI_ZHI_QU("150000","内蒙古自治区"),
	LIAO_NING_SHENG("210000","辽宁省"),
	JI_LIN_SHENG("220000","吉林省"),
	HEI_LONG_JIANG_SHENG("230000","黑龙江省"),
	SHANG_HAI_SHI("310000","上海市"),
	JIANG_SU_SHENG("320000","江苏省"),
	ZHE_JIANG_SHENG("330000","浙江省"),
	AN_HUI_SHENG("340000","安徽省"),
	FU_JIAN_SHENG("350000","福建省"),
	JIANG_XI_SHENG("360000","江西省"),
	SHAN_DONG_SHENG("370000","山东省"),
	HE_NAN_SHENG("410000","河南省"),
	HU_BEI_SHENG("420000","湖北省"),
	HU_NAN_SHENG("430000","湖南省"),
	GUANG_DONG_SHENG("440000","广东省"),
	GUANG_XI_ZHUANG_ZU_ZI_ZHI_QU("450000","广西壮族自治区"),
	HAI_NAN_SHENG("460000","海南省"),
	CHONG_QING_SHI("500000","重庆市"),
	SI_CHUAN_SHENG("510000","四川省"),
	GUI_ZHOU_SHENG("520000","贵州省"),
	YUN_NAN_SHENG("530000","云南省"),
	XI_ZANG_ZI_ZHI_QU("540000","西藏自治区"),
	SHAN_XI_SHENG("610000","陕西省"),
	GAN_SU_SHENG("620000","甘肃省"),
	QING_HAI_SHENG("630000","青海省"),
	NING_XIA_HUI_ZU_ZI_ZHI_QU("640000","宁夏回族自治区"),
	XIN_JIANG_WEI_WU_ER_ZI_ZHI_QU("650000","新疆维吾尔自治区"),
	TAI_WAN_SHENG("710000","台湾省"),
	XIANG_GANG_TE_BIE_XING_ZHENG_QU("810000","香港特别行政区"),
	AO_MEN_TE_BIE_XING_ZHENG_QU("820000","澳门特别行政区");
	
	private String code;
	private String name;
	
    DistributorSaleAreaEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
    
    public static DistributorSaleAreaEnum getByCode(String code) {
        for (DistributorSaleAreaEnum item : DistributorSaleAreaEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

	public static String getNameByCode(String code) {
		DistributorSaleAreaEnum areaEnum = getByCode(code);
		return areaEnum == null ? null : areaEnum.getName();
	}
}
