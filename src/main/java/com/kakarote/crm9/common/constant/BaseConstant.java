package com.kakarote.crm9.common.constant;

import com.kakarote.crm9.utils.BaseUtil;

import java.io.Serializable;

/**
 * 一些基础配置
 */
public class BaseConstant implements Serializable {

    public static final String NAME = "72CRM";

    public static final String VERSION = "1.0.0";

    private static final long serialVersionUID = 1L;
    /**
     * 默认的上传文件路径
     */
    public final static String UPLOAD_PATH = BaseUtil.isWindows() ? "D:/upload/" : BaseUtil.isDevelop() ? "~/work/crm/upload" : "/home/admin/upload/";

    /**
     * 默认文件映射路径
     */
    public final static String BASE_PATH = "http://127.0.0.1:8080/";

    /**
     * 角色类型列表
     */
    public static final Integer[] ROLE_TYPES = {1, 2, 3, 4, 5, 0};

    /**
     * 超级管理员的roleId
     */
    public static final Integer SUPER_ADMIN_ROLE_ID = 1;

    /**
     * 最终的超级管理员ID，不可被删除
     */
    public static final Long SUPER_ADMIN_USER_ID = 3L;
    /**
     * 查询数据权限递归次数
     */
    public static final int AUTH_DATA_RECURSION_NUM = 20;

    /**
     * 超级管理员角色
     */
    public static final String SUPER_ADMIN_ROLE_NAME = "超级管理员";

    /**
     * 客服角色
     */
    public static final String CUSTOMER_SERVICE_ROLE_NAME = "客服";

    /**
     * 市场角色
     */
    public static final String MARKET_ROLE_NAME = "市场";

    /**
     * 销售运营
     */
    public static final String SALES_OPERATIONS = "销售运营";

    /**
     * 销售支持
     */
    public static final String SALSE_SUPPORT = "销售支持";

    /**
     * 主键冲突异常名称
     */
    public static final String MYSQL_INTEGRITY_CONSTRAINT_VIOLATION_EXCEPTION_NAME = "MySQLIntegrityConstraintViolationException";

    public static class CommonInteger {
        public static final Integer ZERO = 0;
        public static final Integer ONE = 1;
        public static final Integer TWO = 2;
        public static final Integer THREE = 3;
        public static final Integer FOUR = 4;
    }

    public static class OpenState {
        public static final Integer OPEN = 1;
        public static final Integer CLOSE = 2;
    }
}
