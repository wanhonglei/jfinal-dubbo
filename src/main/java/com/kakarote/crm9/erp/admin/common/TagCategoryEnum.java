package com.kakarote.crm9.erp.admin.common;

/**
 * 标签枚举类
 * @author yue.li
 * @date 2019/11/19
 */
public enum TagCategoryEnum {
    /**
     * TagCategoryEnum
     */
    ACCURACY_REQUIREMENTS_KEY("精度需求", "accuracyRequirements"),
    CUSTOMER_GRADE_KEY("客户等级", "customerGrade"),
    CUSTOMER_QUALITY_KEY("客户性质", "customerQuality"),
    CUSTOMER_TYPE_KEY("客户类型", "customerType"),
    DISTRIBUTOR_KEY("分销商等级", "distributor"),
    ROLE_KEY("角色", "role"),
    CUSTOMER_INDUSTRY_KEY("客户行业", "customerIndustry"),
    PARTNER_KEY("生态伙伴", "partner"),
    LOSE_REASON_KEY("原因", "loseReason"),
    WIN_RATE_KEY("赢率", "winRate"),
    LEADS_PUBLIC_REASON_KEY("线索放入公海原因", "leadsPublicReason"),
    CUSTOMER_PUBLIC_REASON_KEY("客户进入公海原因", "CustomerPublicReason"),
    INDUSTRY_KEY("行业", "industry"),
    ;

    private String name;
    private String types;
    TagCategoryEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (TagCategoryEnum tagCategoryEnum : TagCategoryEnum.values()) {
            if (tagCategoryEnum.getTypes().equals(types)) {
                return tagCategoryEnum.name;
            }
        }
        return null;
    }
    public String getName() {
        return name;
    }

    public String getTypes() {
        return types;
    }

    public static String getTypeByName(String name) {
        TagCategoryEnum[] items = TagCategoryEnum.values();
        for(TagCategoryEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
