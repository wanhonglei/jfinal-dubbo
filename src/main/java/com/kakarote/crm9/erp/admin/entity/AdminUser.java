package com.kakarote.crm9.erp.admin.entity;

import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.erp.admin.entity.base.BaseAdminUser;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * AdminUser.
 *
 * @author yue.li
 * @create 2019/11/27 10:00
 */
@Data
public class AdminUser extends BaseAdminUser<AdminUser> {
	public static final AdminUser dao = new AdminUser().dao();
	/**
	 * 查询开始时间
	 */
	private String startTime;
	/**
	 * 查询结束时间
	 */
	private String endTime;
	/**
	 * 用户角色列表
	 */
	private List<Integer> roles;
	/**
	 * 用户角色名称列表
	 */
    private List<String> roleNames;

	/**
	 * 根据用户ID查询用户名称
	 * @param userId
	 * @return
	 */
	public String queryNameByUserId(Long userId) {
		if (Objects.isNull(userId)) {
			return null;
		}
		return Db.queryStr(Db.getSql("admin.user.queryNameByUserId"), userId);
	}
}
