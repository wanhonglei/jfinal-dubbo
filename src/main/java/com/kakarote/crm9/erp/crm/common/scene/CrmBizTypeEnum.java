package com.kakarote.crm9.erp.crm.common.scene;

/**
 * Crm Biz Type Enum
 *
 * @author hao.fu
 * @since 2019/12/27 17:30
 */
public enum CrmBizTypeEnum {
    /**
     * CRM 业务模块枚举值
     */
    LEADS(0, "线索", LeadsSceneEnum.class.getEnumConstants()),
    CUSTOMER(1, "客户", CustomerSceneEnum.class.getEnumConstants()),
    CONTACTS(2, "联系人", ContactSceneEnum.class.getEnumConstants()),
    WEBSITE_POOL(3, "网站客户池", SiteCustomerSceneEnum.class.getEnumConstants()),
    BUSINESS(4, "商机", BusinessSceneEnum.class.getEnumConstants()),
    RECEIVABLES(5, "回款", CrmPaymentSceneEnum.class.getEnumConstants()),
    NOTES(6, "联系小记", NotesSceneEnum.class.getEnumConstants()),
    SIGN_IN(7, "签到", SignInSceneEnum.class.getEnumConstants());

    private int bizId;
    private String name;
    private ISceneEnum[] sceneEnums;

    CrmBizTypeEnum(int bizId, String name, ISceneEnum[] sceneEnum) {
        this.name = name;
        this.bizId = bizId;
        this.sceneEnums = sceneEnum;
    }

    public int getBizId() {
        return bizId;
    }

    public String getName() {
        return name;
    }

    public ISceneEnum[] getSceneEnums() {
        return sceneEnums;
    }

    /**
     * Return CRM business enum by business id.
     * @param bizId business id
     * @return Enum
     */
    public static CrmBizTypeEnum getBizEnumByBizId(int bizId) {
        CrmBizTypeEnum[] items = CrmBizTypeEnum.values();
        for (CrmBizTypeEnum item : items) {
            if (item.getBizId() == bizId) {
                return item;
            }
        }
        return null;
    }
}
