package com.kakarote.crm9.erp.crm.constant;


/**
 * crm constants.
 *
 * @author hao.fu
 */
public interface CrmConstant {

    /**线索生成批次号类型*/
    String LEADS_TYPE = "leads";

    /**删除标识为是*/
    int DELETE_FLAG_YES = 1;

    /**删除标识为否*/
    int DELETE_FLAG_NO = 0;

    /**返回成功标识*/
    String SUCCESS_CODE = "200";

    /**部门*/
    String STRUCTURE = "structure";

    Long ROOT_DEPARTMENT_ID = 0L;

    /**千寻位置英文缩写*/
    String QXWZ = "qxwz";

    /**客户类型-个人*/
    String USER_TYPE_PERSONAL = "0";

    /**客户类型-公司*/
    String USER_TYPE_COMPANY = "1";

    /**常量标识0*/
    String ZERO_FLAG = "0";

    /**常量标识1*/
    String ONE_FLAG = "1";

    /**常量标识2*/
    String TWO_FLAG = "2";

    /**常量标识3*/
    String THREE_FLAG = "3";

    String BUC_AUTH_TOKEN_KEY = "buc-auth-token";

    /**所有标签*/
    String ALL_TABS = "allTabs";

    /**网站事业部标签*/
    String WEBSITE_TABS = "websiteTabs";

    /**BUC用户key*/
    String BUC_USER_KEY = "sso_auth_user";

    /**客户放入部门客户池*/
    String DEPT_POOL = "客户放入部门客户池";

    /**将客户放入公海*/
    String PUBLIC_POOL = "将客户放入公海";

    /**将商机放入公海**/
    String BUSINESS_PUBLIC_POOL = "将商机放入公海";

    /**部门线索池联系小计设置天数*/
    String LEADS_DEPT_POOL_CONTACT_SUBTOTAL_SETTING = "leadsDeptPoolContactSubtotalSetting";

    /**部门线索池未转化设置天数*/
    String LEADS_DEPT_POOL_NOT_TRANSFORM_SETTING = "leadsDeptPoolNotTransformSetting";

    /**线索公池联系小计设置天数*/
    String LEADS_PUBLIC_POOL_SETTING = "leadsPublicPoolSetting";

    Long THE_DAY_BEFORE_NOW = 1L;

    String WEB_SITE = "网站电销角色";

    String BUC_SERVER_URL_KEY = "buc.server.url";

    /**客户新建*/
    String CUSTOMER_NEW = "客户新建";

    /**线索转化*/
    String LEADS_TRANSFORM = "线索转化";

    /**官网注册*/
    String WEB_SITE_ORIGIN = "官网注册";

    /**推荐事业部*/
    String RECOMMEND_BUSINESS = "推荐事业部";

    String SERVLET_MAPPING_ALL = "/*";

    String MOBILE_URL_PREFIX = "/crm-mobile/*";

    String JFINAL_FILTER_PARAM = "configClass";

    String UNDERTOW_SOURCE_PATH = "src/main/webapp,";

    String CRM_SERVICE_COLUMBUS_CODE = "crm-service";

    String CRM_SR_CODE = "crmservice";

    String UNDERTOW_CONFIG_FILE = "config/undertow.txt";

    String CRM_JFINAL_FILTER_NAME = "jfinal";

    String CRM_SCENE_SUB_OWNER = "下属负责的";

    /**企业客户*/
    String BUSINESS_CLIENTS = "企业客户";

    /**线索来源 自定义字段*/
    String LEADS_ORIGIN = "线索来源";

    /**客户级别*/
    String CUSTOMER_LEVEL = "客户级别";

    /**客户行业*/
    String CUSTOER_INDUSTRY = "行业";

    /**部门*/
    String DEPT = "部门";

    /**人员*/
    String USER = "人员";

    /**库类型*/
    String STORAGE_TYPE = "库类型";

    /**客户来源*/
    String CUSTOMER_ORIGIN = "客户来源";

    String PUBLIC_SAFETY = "公共安全事业部";
    String FUTURE_CITY = "未来之城事业部";
    String POWER = "电网事业部";
    String DRIVER = "智能驾驶事业部";

    /**需求精度*/
    String ACCURACY_REQUIREMENTS = "accuracyRequirements";

    /**客户等级*/
    String CUSTOMER_GRADE = "customerGrade";

    /**分销等级*/
    String DISTRIBUTOR = "distributor";

    /**客户类型*/
    String CUSTOMER_TYPE = "customerType";

    /**线索来源*/
    String LEAD_COME = "leadCome";

    String CRM_CUSTOMER = "crm_customer";
    String CRM_CONTACTS = "crm_contacts";
    String CRM_BUSINESS = "crm_business";
    String CRM_LEADS = "crm_leads";

    String CRM_SITE_USER_TYPE_PERSONAL = "个人客户";

    String CRM_SCENE_MY_OWN = "我负责的";

    String CRM_SCENE_MY_TAKE_PART_IN = "我参与的";

    String QUERY_BY_ID = "queryById";
    String INFORMATION = "information";
    String DELETE_BY_IDS = "deleteByIds";

    String QUERY_LEADS_PAGE_LIST = "queryLeadsPageList";
    String QUERY_CUSTOMERS_PAGE_LIST = "queryCustomersPageList";
    String QUERY_CONTACTS_PAGE_LIST = "queryContactsPageList";
    String QUERY_CUSTOMERS_WEB_SITE_PAGE_LIST = "queryCustomersWebSitePageList";
    String QUERY_BUSINESS_PAGE_LIST = "queryBusinessPageList";

    int AUDIT_SUCCESS = 3;

    /**PPL报表名称*/
    String PLAN_REPORT = "plan";

    /**数值类型1*/
    int INTEGER_ONE = 1;
    /**数值类型0*/
    int INTEGER_ZERO = 0;

    String DB_FIELDS_SEPERATOR = ",";

    /**销售日常跟踪报表名称*/
    String SALE_USUAL_REPORT = "saleUsual";

    /**百分号*/
    String PERCENT = "%";

    /**销售日常报表产品结果key*/
    String PRODUCT_REVENUE_MAP = "productRevenueMap";

    /**excel content_type*/
    String CONTENT_TYPE = "application/vnd.ms-excel;charset=utf-8";

    /**excel 字符编码*/
    String UTF = "UTF-8";

    /**联系小计报表名称*/
    String NOTE_REPORT = "note";

    String SIGN_IN_EXCEL_NAME = "crmSignIn";

    /** 分页 */
    Integer DISPLAY_PAGING = 1;

    /** 不分页 */
    Integer DISPLAY_NO_PAGING = 0;

    /**需求描述*/
    String REQUIRE_DESCRIPTION = "需求描述(*)";

    String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**销售日常报表类型*/
    String SALE_USUAL_REPORT_TYPE = "saleUsualReportType";

    /**收入合计(已关联回款计划)*/
    String RELEVANCE_TOTAL_INCOME = "收入合计(已关联回款计划)";

    /**收入合计(未关联回款计划)*/
    String NO_RELEVANCE_TOTAL_INCOME = "收入合计(未关联回款计划)";

    /**商品无对应省份*/
    String NO_PROVINCE = "省份未知";

    String EMAIL_SUBJECT_PAYMENT = "您负责的客户(%s)有一笔新的回款，请绑定相关商机";
    String EMAIL_SUBJECT_PAYMENT_NO_MASTER = "您负责的客户有一笔新的回款到账，该回款暂无对应订单号或合同号，请进行人工干预申请";

    String EMAIL_SUBJECT_PAYMENT_REPEAT = "有一笔新的回款到账，但匹配到多个客户，请判定或协商真实归属后绑定客户";
    String EMAIL_SUBJECT_PAYMENT_REPEAT_NO_MASTER = "有一笔新的回款到账，该回款匹配到多个客户且暂无对应订单号或合同号，请判定或协商真实归属后绑定客户并进行人工干预申请";

    String PAYMENT_NOTIFY_TEMPLATE = "payment-notify.vm";

    /**总计*/
    String TOTAL_INCOME = "总收入";

    /**部门收入报表名称*/
    String DEPT_INCOME_REPORT = "deptIncome";

    /**省份*/
    String PROVINCE = "省份";

    /**客户回款报表名称*/
    String CUSTOMER_RECEIVABLES_REPORT = "customerReceivables";

    /**各省市硬件商品分销信息报表*/
    String DISTRIBUTOR_SALE_AREA_REPORT = "distributorSaleArea";

    /**我的分销商的分销信息报表*/
    String DISTRIBUTOR_BD_REPORT = "distributorBd";

    /**销售数量报表*/
    String DISTRIBUTOR_BD_SALES_QUANTITY = "distributorBdSalesQuantity";

    /**商品回款报表名称*/
    String PRODUCT_RECEIVABLES_REPORT = "productReceivables";

    /**未关联客户*/
    int PAYMENT_STATUS_UNBIND_CUSTOMER = 1;

    /** 已关联客户*/
    int PAYMENT_STATUS_BIND_CUSTOMER = 2;

    /**已关联回款计划*/
    int PAYMENT_STATUS_BIND_RECEIVABLE_PLAN = 3;

    /**空省市区*/
    String PROVINCE_CITY_AREA = "[]";

    /**CRM回款信息状态-消费 */
    int CRM_PAYMENT_TYPE_CONSUME = 1;

    /**CRM回款信息状态-充值 */
    int CRM_PAYMENT_TYPE_RECHARGE = 2;

    /**CRM回款信息状态-退款 */
    int CRM_PAYMENT_TYPE_REFUND = 3;

    /**充值订单号前缀*/
    String PREFIX_RECHARGE_ORDER = "R-";

    /**订单号前缀*/
    String PREFIX_ORDER = "O-";
    
    /**核销单前缀*/
    String BILLING_ORDER = "IVA";

    /** 分销商标识*/
    int IS_DISTRIBUTOR = 1;

    String DISTRIBUTOR_TAB_NAME = "分销信息";

    String DISTRIBUTOR_PROMOTION_TAB_NAME = "推广信息";

    /**操作类型增加*/
    int ADD_OPERATION_TYPE = 0;

    /**操作类型减少*/
    int REDUCE_OPERATION_TYPE = 1;

    /**页码*/
    String PAGE_NO = "1";

    /**页面大小*/
    String PAGE_SIZE = "500";

    /**code成功编码*/
    String CODE = "200";

    /**商品下架*/
    int PRODUCT_PULL_OFF = 0;

    /**商品上架*/
    int PRODUCT_PULL_ON = 1;

    /**一层级部门*/
    Integer ONE_DEPT_LEVEL = 1;

    /**二层级部门*/
    Integer TWO_DEPT_LEVEL = 2;

    /**转化为团队成员*/
    Integer TRANSFER_TYPE = 2;

    /**只读*/
    Integer POWER_ONE = 1;

    /**只写*/
    Integer POWER_TWO = 2;

    /**成功标识*/
    Integer SUCCESS = 0;

    String ADMIN_NOTES_ID_KEY = "notesId";

    String SIGN_IN_HISTORY_ID = "signinHistoryId";

    String PROVINCE_INFO = "省";

    String CITY_INFO = "市";

    String AREA_INFO = "区";

    String IS_MULTIPLE = "是否允许该客户创建多个网站会员账号";

    String YES = "是";

    String NO = "否";

    /**
     * CRM创建流程类型
     */
    String CRM_WORK_FLOW = "crmWorkFlow";

    /**
     * CRM token for mobile
     */
    String CRM_TOKEN_FOR_MOBILE = "crm-token";

    /**
     * Columbus mysql configuration
     */
    String COLUMBUS_DB_MYSQL_JDBC_URL_KEY = "mysql.jdbcUrl";
    String COLUMBUS_DB_MYSQL_USER_KEY = "mysql.user";
    String COLUMBUS_DB_MYSQL_PASSWORD_KEY = "mysql.password";

    /**
     * SR mysql configuration
     */
    String SR_DB_ACCOUNT_KEY = "db_crm.account";
    String SR_DB_ACCOUNT_SECRET_KEY = "db_crm.account_secret";
    String SR_DB_DATABASE_KEY = "db_crm.database";
    String SR_DB_HOST = "db_crm.host";
    String SR_DB_PORT = "db_crm.port";

    String AMAP_STATUS_OK_CODE = "10000";
    String AMAP_AROUND_POI_PARAM_LOCATION = "location";
    String AMAP_AROUND_POI_PARAM_RADIUS = "radius";
    String AMAP_AROUND_POI_PARAM_TYPES = "types";

    String COLUMBUS_AMAP_AROUND_POI_RADIUS_KEY = "amap.around.poi.radius";
    String COLUMBUS_AMAP_AROUND_POI_TYPES_KEY = "amap.around.poi.types";

    String BATCH_ID = "batchId";

    String NO_CRM_PERMISSION = "抱歉，您暂时无CRM权限，请申请哦";
    double EARTH_RADIUS = 6378137;

    /**
     * CRM员工状态：0 禁用
     */
    int ADMIN_USER_STATUS_FORBIDDEN = 0;

    /**
     * CRM员工状态：1 正常
     */
    int ADMIN_USER_STATUS_NORMAL = 1;

    String INVALID_LOGIN_USER = "token异常，请退出重试；若仍不能解决，请联系IT";

    String SENSITIVE_MOBILE = "mobile";

    String TELEMARKETING_DEPT_NAME = "电销运营团队";

    /**
     * 网站客户池保护期设置
     */
    String WEBSITE_CUSTOMER_POOL_SETTING = "websiteCustomerPoolSetting";

    /**
     * 网站业绩计入设置
     */
    String WEBSITE_PERFORMANCE_INCLUDED = "websitePerformanceIncluded";

    /**
     * 目标部门业绩增加设置
     */
    String TARGET_DEPT_PERFORMANCE_INCLUDED = "targetDeptPerformanceIncluded";

    /**
     * 电销团队ID
     */
    String PHONE_SALE_BUSINESS_DEPT_ID = "phoneSaleBusinessDeptId";

    /**
     * 在线运营事业部部门ID
     */
    String ONLINE_BUSINESS_DEPT_ID = "onlineBusinessDeptId";

    /**
     * 数字地信事业部部门ID
     */
    String SURVEY_MAPPING_DEPT_ID = "surveyMappingDeptId";

    /**
     * 默认显示下属XXX场景的角色ID
     */
    String CONFIG_KEY_ROLE_SHOW_SUB_SCENE = "roleShowSubScene";

    String NO_PERMISSIONS = "no Permissions";

    String MOBILE_PATTERN = "^(\\+?0?\\d{2,4}\\-?)?\\d{6,11}$";

    String CRM_USER_INFO = "/crm/userInfo";

    String CRM_SYSTEM_CONFIG = "/crm/sysConfig/querySysConfig";

    String CRM_ROLE_AUTH = "/crm/system/role/auth";

    String GET_MOBILE_TOKEN = "/crm-mobile/auth/getCrmToken";

    String ON_LINE_BUSINESS_DEPT_ID = "在线运营事业部ID";

    /**
     * 客户领取规则需要审批
     */
    Integer NEED_CHECK = 1;

    String YOU_XIAN_ZE_REN_GONG_SI = "有限责任公司";
    String YOU_XIAN_GONG_SI = "有限公司";
    String GONG_SI = "公司";

    /**
     * 用户领取的客户业绩
     */
    String PERFORMANCE_USER_CUSTOMER = "performance:user:customer:";

    /**
     * 商机当前阶段
     */
    String CURRENT = "CURRENT";

    /**
     * 用于随机选的字符
     */
    String BASE_CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
}
