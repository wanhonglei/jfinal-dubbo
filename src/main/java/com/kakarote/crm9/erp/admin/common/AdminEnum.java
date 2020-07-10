package com.kakarote.crm9.erp.admin.common;

/**
 *
 * @author honglei.wan
 */
public enum AdminEnum {
    /**
     * Admin Enum
     */
    LEADS_OWN_KEY("我负责的线索", "leadsOwn"),
    LEADS_DEPT_KEY("事业部线索", "leadsDept"),
    LEADS_PUBLIC_KEY("线索公海", "leadsPublic"),
    CUSTOMER_OWN_KEY("我负责的客户", "customerOwn"),
    CUSTOMER_DEPT_KEY("部门客户池", "customerDept"),
    CUSTOMER_PUBLIC_KEY("网站客户池", "customerPublic"),
    CUSTOMER_TELEMARKETING_KEY("电销客户", "customerTelemarketing"),
    CUSTOMER_DISTRIBUTOR_KEY("分销商负责的客户池", "customerDistributor"),
    CUSTOMER_TAKE_PART("我参与的客户", "customerTakePart"),
    CUSTOMER_GRADE("客户等级", "customerGrade"),
    DISTRIBUTOR("分销商等级", "distributor"),
    CUSTOMER_TYPE("客户类型", "customerType"),
    CUSTOMER_TRADE("行业", "customerTrade"),
    LEADS_TRANSFORM("已转化的线索", "leadsTransform"),
    LEADS_CREATE_KEY("我创建的线索","leadsCreate"),
    CUSTOMER_CREATE_KEY("我创建的客户","customerCreate"),
    LEADS_ALL_KEY("所有线索","leadsAll"),
    LEADS_NOT_RECEIVE_KEY("未领取的线索","leadsNotReceive"),
    LEADS_BACK_KEY("被退回","leadsBack"),
    MY_OWNER_RECEIVABLES_KEY("我负责的回款","myOwnerReceivables"),
    MY_BRANCH_RECEIVABLES_KEY("下属负责的回款","myBranchReceivables"),
    MY_DEPT_RECEIVABLES_KEY("部门的回款","myDeptReceivables"),
    NO_CUSTOMER_RECEIVABLES_KEY("未关联客户的回款","noCustomerReceivables"),
    ALL_RECEIVABLES_KEY("所有回款","allReceivables");

    private String name;
    private String types;
    AdminEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (AdminEnum c : AdminEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
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
}
