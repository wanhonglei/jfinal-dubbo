package com.kakarote.crm9.erp.admin.entity;

import com.kakarote.crm9.common.midway.NotifyService;

import java.util.List;

public class SendEmailEntity {

    /**负责人*/
    private String ownerUserId;

    /**抄送人*/
    private List<String> copyUserList;

    /**消息对象*/
    private NotifyService notifyService;

    /**是否发送负责人leader*/
    private boolean isSendLeader;

    /**发送标题*/
    private String title;

    /**发送内容*/
    private String content;

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public NotifyService getNotifyService() {
        return notifyService;
    }

    public void setNotifyService(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    public boolean isSendLeader() {
        return isSendLeader;
    }

    public void setSendLeader(boolean sendLeader) {
        isSendLeader = sendLeader;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getCopyUserList() {
        return copyUserList;
    }

    public void setCopyUserList(List<String> copyUserList) {
        this.copyUserList = copyUserList;
    }
}
