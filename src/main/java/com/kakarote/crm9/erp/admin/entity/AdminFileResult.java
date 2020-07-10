package com.kakarote.crm9.erp.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AdminFileResult class
 *
 * @author yue.li
 * @date 2020/01/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFileResult {

    /**
     * 批次ID
     */
    private String batchId;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件URL
     */
    private String filePath;

    /**
     * 文件ID
     */
    private Integer fileId;

    /**
     * OSS url
     */
    private String ossUrl;
}
