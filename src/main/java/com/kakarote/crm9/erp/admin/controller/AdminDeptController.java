package com.kakarote.crm9.erp.admin.controller;

import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;

/**
 * @author hmb
 */
public class AdminDeptController extends Controller {

    @Inject
    private AdminDeptService adminDeptService;

    /**
     * @author hmb
     * 设置部门
     * @param adminDept 部门对象
     */
    public void setDept(@Para("") AdminDept adminDept){
//        renderJson(adminDeptService.setDept(adminDept));
        renderJson(R.error("新增、修改部门功能暂未开放，敬请期待"));
    }

    /**
     * @author hmb
     * 查询部门tree列表
     */
    public void queryDeptTree(){
        String type = getPara("type");
        Integer id = getParaToInt("id");
        renderJson(R.ok().put("data",adminDeptService.queryDeptTree(type,id)));
    }

    public void queryFirstLevelDept() {
        renderJson(R.ok().put("data",adminDeptService.queryFirstLevelDept()));
    }

    /**
     * @author zhangzhiwie
     * 查询权限内部门
     */
    public void queryDeptByAuth(){
        renderJson(R.ok().put("data",adminDeptService.queryDeptByAuth(BaseUtil.getUserId())));
    }

    /**
     * @author hmb
     * 删除部门
     */
    public void deleteDept(){
       /* String id = getPara("id");
        renderJson(adminDeptService.deleteDept(id));*/

        renderJson(R.error("删除部门为非法操作"));
    }

    /**
     * 查询事业部部门树
     */
    public void queryBizDeptTree() {
        renderJson(R.ok().put("data",adminDeptService.queryBizDeptTree()));
    }
}
