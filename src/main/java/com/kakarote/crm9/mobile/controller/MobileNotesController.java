package com.kakarote.crm9.mobile.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.CrmEventAnnotation;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.common.CrmEventEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmNotesPageRequest;
import com.kakarote.crm9.erp.crm.service.CrmNotesService;
import com.kakarote.crm9.erp.crm.service.CrmSignInService;
import com.kakarote.crm9.mobile.common.MobileUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

/**
 * MobileNotesController
 *
 * @author yue.li
 * @since 20120/01/17 10:08
 */
@Before(IocInterceptor.class)
public class MobileNotesController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private CrmNotesService crmNotesService;

    @Inject
    private CrmSignInService crmSignInService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    /**
     * 新增或修改联系小计
     * @author yue.li
     */
    @CrmEventAnnotation(crmEventEnum = CrmEventEnum.LATELY_FOLLOW_EVENT)
    public void addOrUpdate(){
        try{
            CrmUser crmUser = MobileUtil.getCrmUser(getRequest());
            if (Objects.nonNull(crmUser) && Objects.nonNull(crmUser.getCrmAdminUser())) {
                logger.info("mobile notes addOrUpdate, user info: " + crmUser.getCrmAdminUser());
            } else {
                logger.info("mobile notes addOrUpdate, user is null");
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
                return;
            }
            JSONObject jsonObject = JSON.parseObject(getRawData());
            R r = crmNotesService.addOrUpdate(jsonObject,crmUser.getCrmAdminUser().getUserId());
            if (r.isSuccess()) {
                String signInHistoryId = jsonObject.getString(CrmConstant.SIGN_IN_HISTORY_ID);
                if(StringUtils.isNotEmpty(signInHistoryId)){
                    // 添加签到下的小计
                    r = crmSignInService.updateSignInNotes(signInHistoryId, r.get(CrmConstant.ADMIN_NOTES_ID_KEY) == null ? "" : r.get(CrmConstant.ADMIN_NOTES_ID_KEY).toString());
                }
            }
            renderJson(r);
        }catch (Exception e){
            logger.error(String.format("mobile notes addOrUpdate error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error("add notes record fail"));
        }
    }

    /**
     * 查询联系小计
     * @author yue.li
     * @param basePageRequest 请求对象
     */
    @Permissions("crm:notes:index")
    public void getNotesList(BasePageRequest<CrmNotesPageRequest> basePageRequest) {
        try{
            CrmUser crmUser = MobileUtil.getCrmUser(getRequest());
            if(Objects.isNull(crmUser)){
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
            }else{
                Page<Record> records = crmNotesService.getNotesList(basePageRequest, crmUser);
                if(Objects.isNull(records)){
                    renderJson(R.error(CrmConstant.NO_PERMISSIONS));
                }else{
                    renderJson(R.ok().put("data", records));
                }
            }
        }catch (Exception e){
            logger.error(String.format("mobile notes getNotesList error msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据小记id查询小记详情
     * @author yue.li
     * @param  noteId 小计ID
     */
    @Permissions("crm:notes:index")
    @NotNullValidate(value = "noteId",message = "小记id不能为空")
    public void noteDetail(@Para("noteId")Integer noteId){
        try{
            CrmUser crmUser = MobileUtil.getCrmUser(getRequest());
            if(Objects.isNull(crmUser)){
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
            }else{
                List<Integer> crmNotesList = crmNotesService.getAuthorizedNotesList(crmUser.getAuthorizedUserIds(),crmUser.getCrmAdminUser().getUserId());
                if(crmNotesList.contains(noteId)) {
                    Record record = crmNotesService.getNoteMobileById(noteId, ossPrivateFileUtil);
                    renderJson(R.ok().put("data",record));
                }else{
                    renderJson(R.error(CrmConstant.NO_PERMISSIONS));
                }
            }
        }catch (Exception e){
            logger.error(String.format("mobile noteDetail msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据小记id删除联系小记
     * @author yue.li
     * @param  noteId 小计ID
     */
    @Permissions("crm:notes:deletenote")
    @NotNullValidate(value = "noteId",message = "小记id不能为空")
    public void delete(@Para("noteId")Integer noteId) {
        try{
            CrmUser crmUser = MobileUtil.getCrmUser(getRequest());
            if(Objects.isNull(crmUser)){
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
            }else{
                List<Integer> crmNotesList = crmNotesService.getAuthorizedNotesList(crmUser.getAuthorizedUserIds(),crmUser.getCrmAdminUser().getUserId());
                if(crmNotesList.contains(noteId)) {
                    boolean delete = crmNotesService.deleteById(noteId, ossPrivateFileUtil, crmUser.getCrmAdminUser());
                    renderJson( delete ? R.ok() : R.error("delete note fail"));
                }else{
                    renderJson(R.error(CrmConstant.NO_PERMISSIONS));
                }
            }
        }catch (Exception e){
            logger.error(String.format("mobile delete notes error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
}
