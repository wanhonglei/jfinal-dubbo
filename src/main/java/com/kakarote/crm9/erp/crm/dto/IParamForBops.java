package com.kakarote.crm9.erp.crm.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/4/16 4:51 下午
 */
@Getter
@Setter
public class IParamForBops {
	/**
	 * 网站用户ID（最多100）
	 */
	private List<Long> userIds;

	/**
	 * 订单类型
	 */
	private Integer orderType;

	/**
	 * 订单状态 || 支付状态 || 兑换码状态
	 */
	private Integer status;

	/**
	 * 计费类型
	 */
	private Integer ratingType;

	/**
	 * 排序方式
	 */
	private List<Sort> sorts;

	/**
	 * 用户ID列表（最多100个）
	 */
	private List<Long> userIdList;

	/**
	 * 发票申请状态
	 */
	private String invoiceRecordStatus;

	/**
	 * 开票渠道
	 */
	private String invoiceChannel;

	/**
	 * 发票申请日期开始
	 */
	private Date creatStartTime;

	/**
	 * 发票申请日期结束
	 */
	private Date creatEndTime;

	/**
	 * 订单ID集合
	 */
	private List<Long> orderIds;

	/**
	 * 订单编号集合
	 */
	private List<String> orderNos;

	/**
	 * 退款状态
	 */
	private Integer refundStatus;

	/**
	 * 交易流水号
	 */
	private List<String> paymentNoList;

	/**
	 * 支付关联的订单号
	 */
	private List<String> orderNoList;

	/**
	 * 交易流水号
	 */
	private List<String> tradeNoList;

	/**
	 * 收款账户
	 */
	private List<String> sellerList;

	/**
	 * 交易关联的银行流水号
	 */
	private List<String> paymentNoBankList;

	/**
	 * 交易关联的微信流水号
	 */
	private List<String> paymentNoWeixinList;

	/**
	 * 交易关联的支付宝流水号
	 */
	private List<String> paymentNoAlipayList;

	@Getter
	@Setter
	public static class Sort{
		/**
		 * 排序优先级
		 */
		private int index;

		/**
		 * 排序字段
		 */
		private String key;

		/**
		 * ture 表示按创建时间倒序，false 表示按创建时间正序
		 */
		private Boolean desc;

	}
}
