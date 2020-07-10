package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.annotation.LogApiOperation;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @author hmb
 */
@Before(IocInterceptor.class)
public class AdminUserController extends Controller {

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private AdminFileService adminFileService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    /**
     * 校验用户是否保存
     */
    @LogApiOperation(methodName = "校验用户客户和商机")
    public void checkUserCustomerAndBusiness() {
        String requestJson = getRawData();
        AdminUser adminUser = JSONObject.parseObject(requestJson, AdminUser.class);
        renderJson(adminUserService.checkUserCustomerAndBusiness(adminUser));
    }

    /**
     * 编辑用户保存
     *
     * @param adminUser
     */
    @LogApiOperation(methodName = "保存编辑用户信息")
    public void setUser(@Para("") AdminUser adminUser) {
        renderJson(adminUserService.setUser(adminUser, getPara("roleIds")));
    }



    /**
     * @author hmb
     * 更新状态
     */
    public void setUserStatus(){
        String ids = getPara("userIds");
        String status = getPara("status");
        renderJson(adminUserService.setUserStatus(ids,status));
    }

    /**
     * @author hmb
     * 查询系统用户列表
     * @param basePageRequest 分页对象
     */
    public void queryUserList(BasePageRequest<AdminUser> basePageRequest){
        String roleId = getPara("roleId");
        String searchKey = getPara("keyword");
        renderJson(adminUserService.queryUserList(basePageRequest,roleId, searchKey));
    }

    /**
     * @author hmb
     * 重置密码
     */
    public void resetPassword(){
        String ids = getPara("userIds");
        String pwd = getPara("password");
        renderJson(adminUserService.resetPassword(ids,pwd));
    }

    /**
     * @author hmb
     * 查询上级列表
     */
    public void querySuperior(){
        String realName = getPara("realName");
        renderJson(adminUserService.querySuperior(realName));
    }

    /**
     * 查询所用用户列表
     * @author hmb
     */
    public void queryAllUserList(){
        renderJson(adminUserService.queryAllUserList());
    }
    /**
     * @author zxy
     * 查询系统所有用户名称
     */
    public void queryListName(@Para("search") String search){
        renderJson(adminUserService.queryListName(search));
    }
    /**
     * @author zxy
     * 查询部门属用户列表
     */
    public void queryListNameByDept(@Para("name") String name){
        renderJson(adminUserService.queryListNameByDept(name));
    }

    /**
     * 查询当前登录的用户
     * @author zhangzhiwei
     */
    public void queryLoginUser(){
        AdminUser adminUser = BaseUtil.getUser();
        if (Objects.isNull(adminUser)) {
            renderJson(R.error(HttpStatus.SC_MOVED_TEMPORARILY, "Please login first!"));
        } else {
            adminUser = adminUserService.resetUser(adminUser, BaseUtil.getToken(getRequest()));
            renderJson(R.ok().put("data", adminUser));
        }
    }

    public void updateImg(){
        String prefix= BaseUtil.getDate();
        CrmUser crmUser = BaseUtil.getCrmUser();
        UploadFile uploadFile=getFile("file",prefix);
        String ossUrl = ossPrivateFileUtil.uploadToOss(uploadFile, Objects.nonNull(crmUser) ? crmUser.getCrmAdminUser().getNum() : null);

        R r=adminFileService.upload(uploadFile,null,"file", ossUrl, ossPrivateFileUtil,crmUser);
        if(r.isSuccess()){
            String url= (String) r.get("url");
            if(adminUserService.updateImg(url,getParaToLong("userId"))){
                renderJson(R.ok());
                return;
            }
        }
        renderJson(R.error("修改头像失败"));
    }

    public void updatePassword(){
        String oldPass=getPara("oldPwd");
        String newPass=getPara("newPwd");
        AdminUser adminUser=BaseUtil.getUser();
        if(!BaseUtil.verify(adminUser.getUsername()+oldPass,adminUser.getSalt(),adminUser.getPassword())){
            renderJson(R.error("密码输入错误"));
            return;
        }
        adminUser.setPassword(newPass);
        boolean b=adminUserService.updateUser(adminUser);
        if(b){
            Redis.use().del(BaseUtil.getToken());
            removeCookie(CrmConstant.BUC_AUTH_TOKEN_KEY);
        }
        renderJson(R.isSuccess(b));
    }

    @NotNullValidate(value = "realname",message = "姓名不能为空")
    @NotNullValidate(value = "username",message = "用户名不能为空")
    public void updateUser(@Para("")AdminUser adminUser){
        boolean b=adminUserService.updateUser(adminUser);
        renderJson(R.isSuccess(b,"修改信息失败"));
    }

    /**
     *
     * @author zhangzhiwei
     * @param id 用户ID
     * @param username 用户新账号
     * @param password 用户新密码
     */
    @NotNullValidate(value = "username",message = "账号不能为空")
    @NotNullValidate(value = "password",message = "密码不能为空")
    @NotNullValidate("id")
    public void usernameEdit(@Para("id")Integer id,@Para("username")String username,@Para("password")String password){
        renderJson(adminUserService.usernameEdit(id,username,password));

    }

    public void queryUserByDeptId(@Para("deptId")Integer deptId){
        renderJson(R.ok().put("data",adminUserService.queryUserByDeptId(deptId)));;
    }

    /**
     * 获取当前登录用户所在部门
     * @author liyue
     */
    public void getDeptInfoByUserName(@Para("userName")String userName){
        renderJson(R.ok().put("data",adminUserService.getDeptInfoByUserName(userName)));;
    }

    /**
     * 根据场景编码查询用户列表
     * @param keyword
     * @param sceneCode
     * @return
     */
    public void queryUserListBySceneCode(String keyword, String sceneCode,Long deptId) {
        try {
            renderJson(R.okWithData(adminUserService.queryUserListBySceneCode(keyword, sceneCode, deptId)));
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }
}
