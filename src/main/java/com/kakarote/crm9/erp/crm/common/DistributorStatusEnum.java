package com.kakarote.crm9.erp.crm.common;

import java.util.Objects;

/**
 * @Author: honglei.wan
 * @Description:分销商枚举
 * @Date: Create in 2020/5/14 11:35 上午
 */
public enum DistributorStatusEnum {
	/**
	 * 分销商枚举
	 */
	DISTRIBUTOR("distributor", "分销商","分销商"),
	LEVEL_TWO_DISTRIBUTOR_PARTNER("distributorPartnerL2", "二级分销伙伴", "二级分销商"),
	DISTRIBUTOR_PARTNER_TERMINAL_USER("terminalUser", "分销伙伴终端用户","终端用户"),
	;

	public static DistributorStatusEnum getByPromotionTag(String promotionTag) {
		for (DistributorStatusEnum value : values()) {
			if (Objects.equals(value.getPromotionTag(), promotionTag)) {
				return value;
			}
		}
		return null;
	}

	public static DistributorStatusEnum getByCode(String code) {
		for (DistributorStatusEnum value : values()) {
			if (Objects.equals(value.getCode(), code)) {
				return value;
			}
		}
		return null;
	}

	private String code;
	private String desc;
	private String promotionTag;

	DistributorStatusEnum(String code, String desc, String promotionTag) {
		this.code = code;
		this.desc = desc;
		this.promotionTag = promotionTag;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

	public String getPromotionTag() {
		return promotionTag;
	}
}
