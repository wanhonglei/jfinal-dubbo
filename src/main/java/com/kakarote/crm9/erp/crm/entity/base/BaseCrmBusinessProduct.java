package com.kakarote.crm9.erp.crm.entity.base;

import com.jfinal.plugin.activerecord.CrmModel;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseCrmBusinessProduct<M extends BaseCrmBusinessProduct<M>> extends CrmModel<M> implements IBean {

	public M setRId(Long rId) {
		set("r_id", rId);
		return (M)this;
	}

	public Long getRId() {
		return getLong("r_id");
	}

	public M setBusinessId(Integer businessId) {
		set("business_id", businessId);
		return (M)this;
	}

	public Integer getBusinessId() {
		return getInt("business_id");
	}

	public M setProductId(Integer productId) {
		set("product_id", productId);
		return (M)this;
	}

	public Integer getProductId() {
		return getInt("product_id");
	}

	public M setPrice(java.math.BigDecimal price) {
		set("price", price);
		return (M)this;
	}

	public java.math.BigDecimal getPrice() {
		return get("price");
	}

	public M setSalesPrice(java.math.BigDecimal salesPrice) {
		set("sales_price", salesPrice);
		return (M)this;
	}

	public java.math.BigDecimal getSalesPrice() {
		return get("sales_price");
	}

	public M setNum(Integer num) {
		set("num", num);
		return (M)this;
	}

	public Integer getNum() {
		return getInt("num");
	}

	public M setDiscount(Integer discount) {
		set("discount", discount);
		return (M)this;
	}

	public Integer getDiscount() {
		return getInt("discount");
	}

	public M setSubtotal(java.math.BigDecimal subtotal) {
		set("subtotal", subtotal);
		return (M)this;
	}

	public java.math.BigDecimal getSubtotal() {
		return get("subtotal");
	}

	public M setUnit(String unit) {
		set("unit", unit);
		return (M)this;
	}

	public String getUnit() {
		return getStr("unit");
	}

	public M setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
		return (M)this;
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public M setUpdateTime(java.util.Date updateTime) {
		set("update_time", updateTime);
		return (M)this;
	}

	public java.util.Date getUpdateTime() {
		return get("update_time");
	}

}
