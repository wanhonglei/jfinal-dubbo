package com.kakarote.crm9.erp.crm.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/16 5:22 下午
 */
@Data
public class BackendOrderInfo implements Serializable {

    private static final long serialVersionUID = -2137698018203832875L;

    //订单id
    private Long orderId;

    //订单号
    private String orderNo;

    //用户id
    private Long userId;

    //用户名
    private String userName;

    //创建时间
    private String gmtCreate;

    //支付时间
    private String payTime;

    //下单BD工号
    private String staffNo;

    //订单到期时间
    private String payDeadline;

    //笔订单应付金额-各总折扣后的实付金额
    private BigDecimal needPay;

    //优惠券抵扣金额
    private BigDecimal couponPay;

    //该笔订单的总金额 没有折扣的原价
    private BigDecimal totalPay;

    //阶梯计费抵扣金额
    private BigDecimal ladderPay;

    //支付类型
    private Integer payType;

    //支付类型Desc
    private String payTypeDesc;

    //订单类型
    private Integer orderType;

    //订单类型Desc
    private String orderTypeDesc;

    //订单状态
    private Integer status;

    //订单状态 Desc
    private String statusDesc;

    //订单退款状态
    private Integer refundStatus;

    //订单退款状态 Desc
    private String refundStatusDesc;

    //
    private Integer channel;

    //下单渠道 Desc
    private String channelDesc;

    //支付渠道
    private Integer payChannel;

    //支付渠道 Desc
    private String payChannelDesc;

    //取消渠道
    private String cancelChannel;

    //取消渠道Desc
    private String cancelChannelDesc;

    //支付流水号
    private String paymentNo;

    //商户号
    private String merchantCode;

    //快递标志
    private Boolean express;

    //订单开票状态
    private Integer invoiceStatus;

    //订单开票状态描述
    private String invoiceStatusDesc;

    //备注
    private String comment;

    //大客户下单BD姓名
    private String staffName;

    //大客户下单流程号
    private String oaFlowNo;

    //标签
    private String tag;

    //第三方订单号
    private String externalOrderId;

    //OA流程编号
    private String oaNumber;
}
