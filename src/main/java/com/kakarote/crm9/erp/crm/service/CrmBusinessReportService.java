package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.admin.vo.DataPermissionVO;
import com.kakarote.crm9.erp.crm.acl.dataauth.CrmDataAuthEnum;
import com.kakarote.crm9.erp.crm.common.CrmBusinessShareholderRelationEnum;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessReport;
import com.kakarote.crm9.utils.BaseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: honglei.wan
 * @Description: CRM 商机报表
 * @Date: Create in 2020/5/26 9:51 上午
 */
@Slf4j
public class CrmBusinessReportService {

	@Inject
	private AdminDeptService adminDeptService;

	@Inject
	private AdminUserService adminUserService;

	/**
	 * 千寻高层角色ID
	 */
	private static final Integer QX_LEADER_ROLE_ID = 32;

	/**
	 * 查看商机报表
	 * @param basePageRequest
	 * @return
	 */
	public Page<Record> queryBusinessReportList(BasePageRequest<CrmBusinessReport> basePageRequest) {
		CrmBusinessReport requestData = basePageRequest.getData() == null ? new CrmBusinessReport() : basePageRequest.getData();
		//当前登陆用户
		AdminUser currentUser = BaseUtil.getUser();

		if (StringUtils.isBlank(requestData.getShareholderRelationCode())){
			//如果是千寻高层 或者 超管，可以查询所有商机
			if (!currentUser.getRoles().contains(QX_LEADER_ROLE_ID) && !currentUser.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
				return new Page<>();
			}
		}

		if (StringUtils.isNotBlank(requestData.getCreateStartTime())){
			try {
				DateUtil.parse(requestData.getCreateStartTime(), DatePattern.NORM_DATE_PATTERN);
			} catch (Exception e) {
				log.info("业务异常:{}",e.getMessage());
				throw new CrmException("参数：createStartTime 格式错误",e);
			}
		}

		if (StringUtils.isNotBlank(requestData.getCreateEndTime())){
			try {
				DateUtil.parse(requestData.getCreateEndTime(), DatePattern.NORM_DATE_PATTERN);
			} catch (Exception e) {
				log.info("业务异常:{}",e.getMessage());
				throw new CrmException("参数：createEndTime 格式错误",e);
			}
		}

		Map<String, Object> beanToMap = BeanUtil.beanToMap(requestData);

		//数据权限
		DataPermissionVO dataPermission = adminUserService.queryDataPermission(BaseUtil.getUserId());
		if (dataPermission.getLevel() == null) {
			return new Page<>();
		}
		//非全部类型，用户ID为空直接返回
		if (!CrmDataAuthEnum.ALL_TYPE_KEY.getTypes().equals(dataPermission.getLevel()) && CollectionUtils.isEmpty(dataPermission.getUserIdList())) {
			return new Page<>();
		}
		//全部类型不需要判断owner，非全部类型判断owner
		if (!CrmDataAuthEnum.ALL_TYPE_KEY.getTypes().equals(dataPermission.getLevel())) {
			List<Long> subUserIds = dataPermission.getUserIdList();
			beanToMap.put("ownerUserIds", subUserIds);
		}

		if (requestData.getDeptId() != null){
			beanToMap.put("deptIds",adminDeptService.getDeptAndBranchDept(requestData.getDeptId()));
		}
		if (StringUtils.isNotBlank(requestData.getShareholderRelationCode())){
			beanToMap.put("shareholderRelationCodes",Arrays.stream(requestData.getShareholderRelationCode().split(",")).map(Integer::valueOf).collect(Collectors.toList()));
		}

		SqlPara sqlPara = Db.getSqlPara("crm.business.queryBusinessReportListFrom", beanToMap);
		String orderKey = StringUtils.isBlank(requestData.getOrderKey()) ? "ccb.update_time" : requestData.getOrderKey();
		String orderType = StringUtils.isBlank(requestData.getOrderType()) ? "desc" : requestData.getOrderType();
		//SELECT SQL
		String fromSql = sqlPara.getSql() + " order by " + orderKey + " " + orderType;
		//QUERY
		Page<Record> paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSql("crm.business.queryBusinessReportListSelect"), fromSql, sqlPara.getPara());
		paginate.getList().forEach(o -> {
			AdminDept adminDept = adminDeptService.getBusinessDepartmentPOByDeptId(o.getLong("deptId"));
			if (Objects.nonNull(adminDept)){
				o.set("bizGroupDeptName",adminDept.getName());
				o.set("deptId",adminDept.getDeptId());
			}else{
				o.set("bizGroupDeptName",null);
			}
			o.set("shareholderRelationName", CrmBusinessShareholderRelationEnum.getName(o.getInt("shareholder_relation")));
		});

		return paginate;
	}
}
