package com.kakarote.crm9.erp.crm.dto;

import cn.hutool.core.date.DateTime;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.common.CrmOperateChannelEnum;
import com.kakarote.crm9.erp.crm.common.CrmOperateChannelEventEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @author liming.guo
 */
@Data
@Builder
public class CrmChangeLogDto {

    /**
     * 请求的唯一requestId
     */
    private String requestId;

    private CrmOperateChannelEnum channelEnum;

    private String channelEvent;

    private String changeHistory;

    private String fromId;

    private Long operatorId;

    private DateTime operateTime;

    private Long oldOwnerUserId;

    private Long oldOwnerUserDeptId;

    private Long newOwnerUserId;

    private Long newOwnerUserDeptId;

}
