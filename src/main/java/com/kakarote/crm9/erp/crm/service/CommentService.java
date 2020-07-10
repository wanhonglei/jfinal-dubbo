package com.kakarote.crm9.erp.crm.service;

import com.kakarote.crm9.erp.crm.entity.TaskComment;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论内容服务类
 * @author honglei.wan
 */
public class CommentService {

    /**
     * 新增、编辑评论
     * @param comment
     * @return
     */
    public R setComment(TaskComment comment) {
        boolean bol;
        if ( comment.getCommentId () == null ){
            comment.setCreateTime(new Date());
            comment.setUserId(BaseUtil.getUserId().intValue());
            bol = comment.save();
        }else {
            bol = comment.update ();
        }
        if(comment.getMainId() == null){
            comment.setMainId(0);
        }
        return bol ? R.ok().put("data",comment) : R.error();
    }

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    public R deleteComment(Integer commentId){
        Db.delete(Db.getSql("crm.record.deleteCommentByMainId"), commentId);
        return new TaskComment ().dao ().deleteById ( commentId )?R.ok ():R.error ();
    }

    /**
     * 查询评论
     * @param typeId
     * @param type
     * @return
     */
    public List<Record> queryCommentList(String typeId, String type) {
        List<Record> recordList = Db.find(Db.getSql("crm.record.queryRecordOfComment"), typeId, type);
        if(recordList == null || recordList.size() == 0){
            return new ArrayList<>();
        }
        recordList.forEach(record -> {
            if ( record.getStr ( "user_id" ) != null && !"".equals ( record.getStr ( "user_id" ) )){
                record.set("user",Db.findFirst ( Db.getSql("crm.record.queryUserByUserId") ,record.getStr ( "user_id" )));
            }
            if ( record.getStr ( "pid" ) != null && !"0".equals ( record.getStr ( "pid" ) ) && !"".equals ( record.getStr ( "pid" ) )){
                record.set("replyUser",Db.findFirst ( Db.getSql("crm.record.queryUserByUserId"),record.getStr ( "pid" )));
            }
        });
        Map<Integer,List<Record>> pMap = recordList.stream().collect(Collectors.groupingBy(record -> record.getInt("main_id")));
        recordList = pMap.get(0);
        recordList.forEach(record -> {
            Integer commentId = record.getInt("comment_id");
            if(pMap.get(commentId)!= null){
                record.set("childCommentList",pMap.get(commentId));
            }else {
                record.set("childCommentList",new ArrayList<>());
            }
        });
        return recordList;
    }
}
