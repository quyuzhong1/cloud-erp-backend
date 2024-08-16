package com.erp.model.dmp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

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
    @ColumnWidth(30)
    @ExcelProperty(value = "来源平台名称", index = 0)
    private String sourcePlatformName;

    /**
     * 目标平台名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "目标平台名称", index = 1)
    private String targetPlatformName;

    /**
     * 同步类型
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "同步类型", index = 2)
    private String syncTypeName;

    /**
     * 单据类型
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "单据类型", index = 3)
    private String sourceTypeName;

    /**
     * 单据编号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "单据编号", index = 4)
    private String sourceCode;


    /**
     * 创建日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建日期", index = 5)
    private LocalDateTime createTime;

    /**
     * 操作节点
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "操作节点", index = 6)
    private String syncOperateName;

    /**
     * 同步状态名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "同步状态名称", index = 7)
    private String statusName;

    /**
     * 最新推送时间
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "最新推送时间", index = 8)
    private LocalDateTime lastSyncTime;

    /**
     * 推送失败原因
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "推送失败原因", index = 9)
    private String returnMsg;

    /**
     * 第三方编号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "第三方编号", index = 10)
    private String thirdCode;
}
