package com.kakarote.crm9.common.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.erp.admin.service.AdminRoleService;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

/**
 * 权限功能后台拦截
 * @author honglei.wan
 */
public class AuthInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation invocation) {
        Permissions permissions=invocation.getMethod().getAnnotation(Permissions.class);
        if(permissions!=null&&permissions.value().length>0){
            JSONObject jsonObject= Aop.get(AdminRoleService.class).auth(BaseUtil.getUserId());
            List<String> arr=queryAuth(jsonObject, "");
            boolean isRelease=false;
            for (String key : permissions.value()) {
                if(!isRelease){
                    if(arr.contains(key)){
                        isRelease=true;
                    }
                }
            }
            if(!isRelease){
                invocation.getController().renderJson(R.error("无权访问"));
                return;
            }
        }


        //防止水平越权数据校验
        //获取方法名
        String methodName = invocation.getMethodName();
        if(CrmConstant.QUERY_BY_ID.equals(methodName) || CrmConstant.INFORMATION .equals(methodName) || CrmConstant.DELETE_BY_IDS.equals(methodName)){
            String id = "";
            String controllerKey = invocation.getActionKey().split("/")[2];

            Map<String, String[]> parameterMap = invocation.getController().getRequest().getParameterMap();
            // 使用entrySet 代替 keySet
            for (Entry<String, String[]> entry : parameterMap.entrySet()) {
            	String[] values = entry.getValue();
            	id = values[0];
            }
            boolean crmAuth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(controllerKey), "".equals(id) ? Integer.valueOf(0) : Integer.valueOf(id));
            if(crmAuth){
                invocation.getController().renderJson(R.error("无权访问"));
                return;
            }
        }

        //列表查询权限控制
        if(CrmConstant.QUERY_LEADS_PAGE_LIST.equals(methodName) ||
                CrmConstant.QUERY_CUSTOMERS_PAGE_LIST.equals(methodName) ||
                CrmConstant.QUERY_CONTACTS_PAGE_LIST.equals(methodName) ||
                CrmConstant.QUERY_CUSTOMERS_WEB_SITE_PAGE_LIST.equals(methodName) ||
                CrmConstant.QUERY_BUSINESS_PAGE_LIST.equals(methodName)){
            HttpServletRequest request = invocation.getController().getRequest();
            JSONObject json = JSONObject.parseObject(invocation.getController().getRawData());
            if(json != null){
                Integer sceneId = json.getInteger("sceneId");
                boolean crmAuth = AuthUtil.checkPageListPower(sceneId, BaseUtil.getUserId());
                if(crmAuth){
                    invocation.getController().renderJson(R.error("无权访问"));
                    return;
                }
            }
        }
        invocation.invoke();
    }
    @SuppressWarnings("unchecked")
    private List<String> queryAuth(Map<String,Object> map,String key){
        List<String> permissions=new ArrayList<>();
        map.keySet().forEach(str->{
            if(map.get(str) instanceof Map){
                permissions.addAll(this.queryAuth((Map<String, Object>) map.get(str),key+str+":"));
            }else {
                permissions.add(key+str);
            }
        });
        return permissions;
    }
}
