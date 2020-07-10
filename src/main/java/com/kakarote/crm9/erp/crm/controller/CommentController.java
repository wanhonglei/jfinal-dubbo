package com.kakarote.crm9.erp.crm.controller;

import com.jfinal.log.Log;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.erp.crm.service.CommentService;
import com.kakarote.crm9.erp.crm.entity.TaskComment;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import org.apache.commons.lang3.StringUtils;

/**
 * 评论控制类
 * @author honglei.wan
 */
public class CommentController extends Controller{

    @Inject
    private CommentService commentService;
    private Log logger = Log.getLog(getClass());
    /**
     * @author hmb
     * 添加评论或者修改
     * @param comment 评论对象
     */
    @Permissions("crm:notes:reply")
    public void setComment(@Para("") TaskComment comment){
        try{
            if (comment == null){
                renderJson(R.error("参数异常"));
                return;
            }
            if (StringUtils.isBlank(comment.getContent())){
                renderJson(R.error("评论内容不能为空"));
                return;
            }
            if (comment.getContent().length() > 255){
                renderJson(R.error("评论内容不能超过255个字符"));
                return;
            }
            renderJson(commentService.setComment(comment));
        }catch (Exception e){
            logger.error(String.format("setComment oaComment msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }

    }

    /**
     * 删除评论
     */
    @Permissions("crm:notes:deletereply")
    public void deleteComment(){
        try{
            Integer commentId = getParaToInt ( "commentId" );
            renderJson(commentService.deleteComment (commentId));
        }catch (Exception e){
            logger.error(String.format("deleteComment oaComment msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author hmb
     * 查询评论列表
     */
    public void queryCommentList(){
        try{
            String typeId = getPara("typeId");
            String type = getPara("type");
            renderJson(R.ok().put("data",commentService.queryCommentList(typeId,type)));
        }catch (Exception e){
            logger.error(String.format("queryCommentList oaComment msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
}
