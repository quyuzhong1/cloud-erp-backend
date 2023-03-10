package com.erp.model.plm.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.excel.EasyExcelLocalDateConverter;
import com.common.core.excel.EasyExcelLocalTimeConverter;
import com.common.core.excel.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Cloud
 * @Classname ProjectTaskTimeRecordPageVO
 * @Date 2023-01-11 17:53
 */
@Data
@NoArgsConstructor
public class ProjectTaskTimeRecordPageVO implements Serializable {

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称", index = 0)
    private String productName;
    /**
     * spu编号
     */
    @ExcelProperty(value = "SPU", index = 1)
    private String spuNo;
    /**
     * 负责人名称
     */
    @ExcelProperty(value = "负责人名称", index = 2)
    private String chargeName;

    /**
     * 计划工时(天数)
     */
    @ExcelProperty(value = "计划工时(天数)", index = 3)
    private Integer planTaskTime;

    /**
     * 完成任务耗时（天）
     */
    @ExcelProperty(value = "完成任务耗时(天)", index = 4)
    private Integer consumerTime;
    /**
     * 排期任务数量
     */
    @ExcelProperty(value = "排期任务数量", index = 5)
    private Integer planTaskNum;
    /**
     * 总任务数量
     */
    @ExcelProperty(value = "总任务数量", index = 6)
    private Integer totalTaskNum;
    /**
     * 已完成任务数量
     */
    @ExcelProperty(value = "已完成任务数量", index = 7)
    private Integer finishedTaskNum;
    /**
     * 未完成任务数量
     */
    @ExcelProperty(value = "未完成任务数量", index = 8)
    private Integer unfinishedTaskNum;
    /**
     * 延期任务数量
     */
    @ExcelProperty(value = "延期任务数量", index = 9)
    private Integer delayTaskNum;

    /**
     * 产品创建时间
     */
    @ExcelProperty(value = "产品创建时间", index = 10)
    private LocalDate productCreateDate;
    /**
     * 产品立项时间
     */
    @ExcelProperty(value = "产品立项时间", index = 11)
    private LocalDate productApprovalDate;

    private String taskIds;


}
