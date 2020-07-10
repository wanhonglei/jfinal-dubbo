package com.kakarote.crm9.erp.crm.common;

/**
 * 商机股东关系
 *
 * @author honglei.wan
 */
public enum CrmBusinessShareholderRelationEnum {
    /**
     * CrmBusinessShareholderRelationEnum
     */
    ZB_RECOMMEND("中兵介绍项目", 1, "32,33", CrmBusinessShareholderRelationCategoryEnum.ZB),
    ZB_COOPERATION("中兵合作项目", 2, "32,33", CrmBusinessShareholderRelationCategoryEnum.ZB),
    ALI_RECOMMEND("阿里介绍项目", 3, "32,34", CrmBusinessShareholderRelationCategoryEnum.ALI),
    ALI_COOPERATION("阿里合作项目", 4, "32,34", CrmBusinessShareholderRelationCategoryEnum.ALI),
    ALIYUN_RECOMMEND("阿里云合作项目", 8, "32,34", CrmBusinessShareholderRelationCategoryEnum.ALI),
    GH_RECOMMEND("上海国和&上海国际介绍项目", 5, "32", CrmBusinessShareholderRelationCategoryEnum.OTHER),
    GY_RECOMMEND("工银金融介绍项目", 6, "32", CrmBusinessShareholderRelationCategoryEnum.OTHER),
    JW_RECOMMEND("深圳见微介绍项目", 7, "32", CrmBusinessShareholderRelationCategoryEnum.OTHER),
    ;

    private String name;
    private Integer type;
    private String roleIds;
    private CrmBusinessShareholderRelationCategoryEnum category;

    CrmBusinessShareholderRelationEnum(String name, Integer type, String roleIds, CrmBusinessShareholderRelationCategoryEnum category) {
        this.name = name;
        this.type = type;
        this.roleIds = roleIds;
        this.category = category;
    }

    public static String getName(Integer type) {
        for (CrmBusinessShareholderRelationEnum c : CrmBusinessShareholderRelationEnum.values()) {
            if (c.getType().equals(type)) {
                return c.name;
            }
        }
        return "";
    }

    public String getName() {
        return name;
    }

    public Integer getType() {
        return type;
    }

    public String getRoleIds() {
        return roleIds;
    }

    public CrmBusinessShareholderRelationCategoryEnum getCategory() {
        return category;
    }
}
