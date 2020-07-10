package com.kakarote.crm9.erp.crm.common;
/**
 * CrmErrorMessage class
 *
 * @author yue.li
 * @date 2019/12/02
 */
public interface CrmErrorMessage {

    String NEW_TEMPLATE = "请使用最新导入模板";

    String ORDER_NO_IS_NULL = "您好,订单编号不能为空";

    String ORDER_IS_NOT_LEGAL = "您好,订单编号不合法,请输入正确的订单编号:";

    String EXPRESS_COMPANY_IS_NULL = "您好,快递公司不能为空";

    String EXPRESS_NO_IS_NULL = "您好,快递单号不能为空";

    String GOODS_NAME_IS_NULL = "您好,商品名称不能为空";

    String GOODS_SPEC_IS_NULL = "您好,商品规格不能为空";

    String NUM_IS_NULL = "您好,发货数量不能为空";
}
