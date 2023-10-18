package com.erp.model.dmp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author Will
 * @version 1.0
 * @description: 中台同步推送任务表导出DTO
 * @date 2023/10/13 15:40
 */
@Data
public class DmpPushTaskExportExcelDTO {

    /**
     * 来源平台名称
     */
    @ExcelProperty(value = "来源平台名称", index = 0)
    private String sourcePlatformName;

    /**
     * 目标平台名称
     */
    @ExcelProperty(value = "目标平台名称", index = 1)
    private String targetPlatformName;

    /**
     * 来源类型名称
     */
    @ExcelProperty(value = "来源类型名称", index = 2)
    private String sourceTypeName;

    /**
     * 来源单号
     */
    @ExcelProperty(value = "来源类型名称", index = 3)
    private String sourceCode;


    /**
     * 创建日期
     */
    @ExcelProperty(value = "创建日期", index = 4)
    private String createTime;


    /**
     * 最新推送时间
     */
    @ExcelProperty(value = "最新推送时间", index = 5)
    private String lastSyncTime;

    /**
     * 同步状态名称
     */
    @ExcelProperty(value = "同步状态名称", index = 6)
    private String statusName;

    /**
     * 推送失败原因
     */
    @ExcelProperty(value = "推送失败原因", index = 7)
    private String returnMsg;

    /**
     * 同步操作名称
     */
    @ExcelProperty(value = "同步操作名称", index = 8)
    private String syncOperateName;
}
