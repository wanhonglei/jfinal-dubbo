package com.kakarote.crm9.erp.admin.entity.base;

import com.jfinal.plugin.activerecord.CrmModel;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseAdminUser<M extends BaseAdminUser<M>> extends CrmModel<M> implements IBean {

	public M setId(Long id) {
		set("id", id);
		return (M)this;
	}

	public Long getId() {
		return getLong("id");
	}

	public M setUserId(Long userId) {
		set("user_id", userId);
		return (M)this;
	}

	public Long getUserId() {
		return getLong("user_id");
	}

	public M setUsername(String username) {
		set("username", username);
		return (M)this;
	}

	public String getUsername() {
		return getStr("username");
	}

	public M setPassword(String password) {
		set("password", password);
		return (M)this;
	}

	public String getPassword() {
		return getStr("password");
	}

	public M setSalt(String salt) {
		set("salt", salt);
		return (M)this;
	}

	public String getSalt() {
		return getStr("salt");
	}

	public M setImg(String img) {
		set("img", img);
		return (M)this;
	}

	public String getImg() {
		return getStr("img");
	}

	public M setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
		return (M)this;
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public M setHireTime(java.util.Date hireTime) {
		set("hire_time", hireTime);
		return (M)this;
	}

	public java.util.Date getHireTime() {
		return get("hire_time");
	}

	public M setRealname(String realname) {
		set("realname", realname);
		return (M)this;
	}

	public String getRealname() {
		return getStr("realname");
	}

	public M setNum(String num) {
		set("num", num);
		return (M)this;
	}

	public String getNum() {
		return getStr("num");
	}

	public M setMobile(String mobile) {
		set("mobile", mobile);
		return (M)this;
	}

	public String getMobile() {
		return getStr("mobile");
	}

	public M setEmail(String email) {
		set("email", email);
		return (M)this;
	}

	public String getEmail() {
		return getStr("email");
	}

	public M setSex(Integer sex) {
		set("sex", sex);
		return (M)this;
	}

	public Integer getSex() {
		return getInt("sex");
	}

	public M setDeptId(Integer deptId) {
		set("dept_id", deptId);
		return (M)this;
	}

	public Integer getDeptId() {
		return getInt("dept_id");
	}

	public M setPost(String post) {
		set("post", post);
		return (M)this;
	}

	public String getPost() {
		return getStr("post");
	}

	public M setStatus(Integer status) {
		set("status", status);
		return (M)this;
	}

	public Integer getStatus() {
		return getInt("status");
	}

	public M setParentId(Long parentId) {
		set("parent_id", parentId);
		return (M)this;
	}

	public Long getParentId() {
		return getLong("parent_id");
	}

	public M setLeaderNum(String leaderNum) {
		set("leader_num", leaderNum);
		return (M)this;
	}

	public String getLeaderNum() {
		return getStr("leader_num");
	}

	public M setLastLoginTime(java.util.Date lastLoginTime) {
		set("last_login_time", lastLoginTime);
		return (M)this;
	}

	public java.util.Date getLastLoginTime() {
		return get("last_login_time");
	}

	public M setLastLoginIp(String lastLoginIp) {
		set("last_login_ip", lastLoginIp);
		return (M)this;
	}

	public String getLastLoginIp() {
		return getStr("last_login_ip");
	}

	public M setUpdateTime(java.util.Date updateTime) {
		set("update_time", updateTime);
		return (M)this;
	}

	public java.util.Date getUpdateTime() {
		return get("update_time");
	}

}
