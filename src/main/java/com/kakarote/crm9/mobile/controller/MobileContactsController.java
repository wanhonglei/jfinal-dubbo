package com.kakarote.crm9.mobile.controller;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.common.CrmSensitiveEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.service.CrmContactsService;
import com.kakarote.crm9.erp.crm.service.CrmSensitiveAccessLogService;
import com.kakarote.crm9.mobile.common.MobileUtil;
import com.kakarote.crm9.mobile.entity.MobileContactsListRequest;
import com.kakarote.crm9.mobile.service.MobileContactsService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import java.util.List;
import java.util.Objects;

/**
 * Mobile Contacts Controller
 *
 * @author hao.fu
 * @since 2020/1/6 11:55
 */
public class MobileContactsController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private MobileContactsService mobileContactsService;

    @Inject
    private CrmContactsService crmContactsService;

    @Inject
    private CrmSensitiveAccessLogService crmSensitiveAccessLogService;

    /**
     * 获取联系人列表
     * @param basePageRequest {@code BasePageRequest}
     */
    public void getContactsList(BasePageRequest<MobileContactsListRequest> basePageRequest) {
        try{
            if(MobileUtil.isEmptyUser(getRequest())){
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
            }else{
                Page<Record> records = mobileContactsService.queryPageList(basePageRequest, MobileUtil.getCrmUser(getRequest()));
                renderJson(R.ok().put("data", records));
            }
        }catch (Exception e){
            logger.error(String.format("MobileContactsController getContactsList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取联系人详情
     *
     * @param contactsId 联系人id
     */
    @NotNullValidate(value = "contactsId",message = "联系人id不能为空")
    public void getContactsDetail(@Para("contactsId")Integer contactsId) {
        try{
            CrmUser user = MobileUtil.getCrmUser(getRequest());
            if (Objects.nonNull(user) && Objects.nonNull(user.getCrmAdminUser())) {
                logger.info("mobile controller getContactsDetail, user info: " + user.getCrmAdminUser());
                List<Integer> crmContactsList = crmContactsService.getAuthorizedContactsList(user.getAuthorizedUserIds());
                if(crmContactsList.contains(contactsId)) {
                    renderJson(R.ok().put("data", mobileContactsService.getContactsDetail(contactsId)));
                }else{
                    renderJson(R.error(CrmConstant.NO_PERMISSIONS));
                }
            }else {
                logger.info("mobile controller getContactsDetail, user is null");
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
            }
        }catch (Exception e){
            logger.error(String.format("MobileContactsController getContactsDetail msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 新建/更新联系人
     */
    public void addOrUpdateContacts() {
        try{
            CrmUser user = MobileUtil.getCrmUser(getRequest());

            if (Objects.nonNull(user) && Objects.nonNull(user.getCrmAdminUser())) {
                logger.info("mobile controller addContacts, user info: " + user.getCrmAdminUser());
            } else {
                logger.info("mobile controller addContacts, user is null");
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
                return;
            }

            JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(getRawData());
            renderJson(crmContactsService.addOrUpdate(jsonObject, user.getCrmAdminUser().getUserId()));
        }catch (Exception e){
            logger.error(String.format("MobileContactsController addContacts msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取联系人手机号
     */
    @NotNullValidate(value = "contactsId",message = "联系人id不能为空")
    public void getContactsMobile(@Para("contactsId")Integer contactsId) {
        try {
            CrmUser user = MobileUtil.getCrmUser(getRequest());
            if (Objects.nonNull(user) && Objects.nonNull(user.getCrmAdminUser())) {
                logger.info("mobile controller getContactsMobile, user info: " + user.getCrmAdminUser());
                List<Integer> crmContactsList = crmContactsService.getAuthorizedContactsList(user.getAuthorizedUserIds());
                if(crmContactsList.contains(contactsId)) {
                    R result = crmContactsService.getMobileByContactsId(contactsId + "");
                    crmSensitiveAccessLogService.addSensitiveAccessLog(CrmSensitiveEnum.CONTACTS_TELEPHONE, user.getCrmAdminUser().getUsername(), "" + contactsId);
                    renderJson(result);
                }else{
                    renderJson(R.error(CrmConstant.NO_PERMISSIONS));
                }
            }else {
                logger.info("mobile controller getContactsMobile, user is null");
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
            }
        } catch (Exception e){
            logger.error(String.format("MobileContactsController addContacts msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
}
