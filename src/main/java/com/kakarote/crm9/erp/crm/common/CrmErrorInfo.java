package com.kakarote.crm9.erp.crm.common;

/**
 * CrmErrorInfo.
 *
 * @author yue.li
 * @create 2019/11/25 10:00
 */
public interface CrmErrorInfo {

    String PARAMS_NOT_EXSIT = "您好，缺少必要的请求参数";

    String PARAMS_CHECK_FAILD = "您好，请求参数校验不通过";

    String BUSINESS_CLIENTS_NEED = "您好,客户类型只能为企业客户";

    String CUSTOMER_TYPE_IS_NULL = "您好,客户类型不能为空";

    String LEADS_IS_TRANSFORM = "已转化线索不能再次转化";

    String LEADS_COMPANY_EXISTS = "公司名称和客户名称重复";

    String CUSTOMER_EXISTS_PUBLIC_POOL = "该客户已存在,目前归属于网站客户池";

    String CUSTOMER_BELONG_TO_USER = "该客户已存在,目前归属于负责人:";

    String CUSTOMER_BELONG_TO_DEPT = "该客户已存在,目前归属于部门客户池:";

    String BUSINESS_GROUP_NOT_EXSIT = "查询商机组不存在";

    String BUSINESS_GROUP_UPDATE_FAILD = "商机组信息更新失败，请重试";

    String BUSINESS_PRODUCT = "该商机关联了[商品]，无法删除";

    String BUSINESS_RECEIVABLES = "该商机关联了[回款信息]，无法删除";

    String BUSINESS_PRODUCT_RECEIVABLES = "该商机关联了[商品]和[回款信息]，无法删除";

    String BUSINESS_STATUS_NOT_EXSIT = "查询商机阶段不存在";

    String BUSINESS_STATUS_DELETE_MESSAGE = "存在已使用该阶段的商机，不可删除";

    String CONTACTS_NAME_IS_NOT_NULL = "联系人名称不能为空，请修正Excel中的数据重新导入";

    String CONTACTS_CUSTOMER_IS_NOT_NULL = "所属客户不能为空，请修正Excel中的数据重新导入";

    String CONTACTS_MOBILE_IS_NOT_NULL = "手机信息不能为空，请修正Excel中的数据重新导入";

    String CONTACTS_ROLE_IS_NOT_NULL = "角色信息不能为空，请修正Excel中的数据重新导入";

    String CUSTOMER_CONTACTS_NOT_NULL = "该客户关联了[联系人]，无法删除";

    String CUSTOMER_BUSINESS_NOT_NULL = "该客户关联了[商机]，无法删除";

    String CUSTOMER_NOTE_NOT_NULL = "该客户关联了[联系小记]，无法删除";

    String CUSTOMER_CONTACTS_BUSINESS_NOT_NULL = "该客户关联了[联系人]和[商机]，无法删除";

    String CUSTOMER_IS_NOT_NULL = "客户名称不能为空,请修正Excel中的数据重新导入";

    String PROVINCE_NOT_NULL = "省份不能为空，请修正Excel中的数据重新导入";

    String CITY_NOT_NULL = "市不能为空，请修正Excel中的数据重新导入";

    String DEPT = "部门:";

    String DEPT_NOT_NULL = "在系统中不存在，请修正Excel中的数据重新导入";

    String OWNER_USER_NAME = "负责人名称:";

    String OWNER_USER_NAME_NOT_NULL = "名称在系统中不存在，请修正Excel中的数据重新导入";

    String CUSTOMER_GRADE_NOT_NULL = "客户等级不能为空，请修正Excel中的数据重新导入";
    
    String CUSTOMER_GRADE_NOT_EXIST = "客户等级不存在，请修正为以下值：小型；中型；大型；重点";

    String CUSTOMER_TYPE_NOT_NULL = "客户类型不能为空，请修正Excel中的数据重新导入";

    String CUSTOMER_TYPE_NOT_EXIST = "客户类型不存在，请修正为以下值：企业客户";
    
    String CUSTOMER_TYPE_NOT_PERSONAL = "客户类型不能为个人客户，请修改为企业客户";

    String CUSTOMER_UPDATE_FAILD = "客户更新失败";

    String DISTRIBUTOR_NOT_EXIST = "分销商等级不存在，请修正为以下值：一级分销商；二级分销商";
    
    String PARTNER_NOT_EXIST = "生态伙伴不存在，请修正为以下值：分销商；千寻大使；KA特征用户";

    String REQUIRE_DESCRIPTION_NOT_NULL  = "需求描述不能为空，请修正Excel中的数据重新导入";

    String RECEIVABLE_NOT_NULL = "该回款计划已关联了实际回款，不能删除";

    String SITE_MEMBER_INFO = "该官网用户ID已经关联了客户:";

    String SITE_MEMBER_NOT_NULL = "如果需要合并客户请联系产品经理";

    String CUSTOMER_OWNER_USER_IS_NULL = "您好,客户负责人在CRM系统不存在";

    String CUSTOMER_IS_NULL = "您好,客户在CRM系统不存在";

    String DEPT_IS_NULL = "您好,领取人对应的部门不存在";

    String BUSINESS_DEPT_IS_NULL = "您好,领取人对应的事业部不存在";

    String OWNER_NOT_NULL_STORAGE_NULL = "负责人字段填写时，库类型字段必须填写";

    String STORAGE_VALIDATE = "【库类型】不为空时，仅允许填“考察库”和“关联库”之一";

    String USER_USERID_IS_EMPTY = "您好，员工ID不能为空";

    String USER_QUERY_USER_INFO_EMPTY = "您好，查询用户信息不存在";

    String USER_SET_PARENT_IS_ERROR = "该员工的下级员工不能设置为直属上级";

    String TELEPHONE_NOT_NULL  = "联系电话不能为空，请修正Excel中的数据重新导入";
}
