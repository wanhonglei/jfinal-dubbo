package com.kakarote.crm9.erp.admin.entity;

import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.erp.admin.entity.base.BaseAdminDept;

import java.util.Objects;

/**
 * Generated by JFinal.
 * @author honglei.wan
 */
@SuppressWarnings("serial")
public class AdminDept extends BaseAdminDept<AdminDept> {
	public static final AdminDept dao = new AdminDept().dao();

	/**
	 * 根据部门ID查询部门名称
	 * @param deptId
	 * @return
	 */
	public String queryNameByDeptId(Integer deptId) {
		if (Objects.isNull(deptId)) {
			return null;
		}
		return Db.queryStr(Db.getSql("admin.dept.queryNameByDeptId"), deptId);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
}
