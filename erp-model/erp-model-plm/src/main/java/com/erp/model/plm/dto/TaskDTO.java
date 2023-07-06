package com.erp.model.plm.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.FieldValid;
import com.common.core.anno.StateEnumValue;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Lambda
 * @Classname TasdDTO

 * @Date 2023-06-20 19:56
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public  static class TaskPagingParamDTO extends SortDTO {

        /**
         * 搜索关键字
         */
        private String searchKeyword;

        /**
         * 任务条件
         * 1.待完成
         * 2 全部
         * 3.待审核
         */
        private Integer taskCondition;

        /**
         * 分组名 no 不分组
         * product 产品分组
         * planEndTime  计划结束时间
         */
        @StateEnumValue(strValues = {"no","product","planEndTime"}, message = "分组名标示")
        private String groupNameFlag;

        /**
         * 分组的标示 可能是时间 也可能是产品id
         */
        private String groupFlag;


        /**
         * 高级搜索筛选条件
         */
        private TaskSearchDTO  taskSearchDTO;


    }



    @Data
    @NoArgsConstructor
    public static  class TaskExportDTO{

        /**
         * 任务id
         */
        private String taskId;


        /**
         * 任务名称
         */
        @FieldValid(fieldName = "任务名称", isNotBlank = true, maxLength = 50)
        private String name;


        /**
         * 任务类型 0 一般任务 1：审核任务
         */
        private Integer type;

        /**
         * 任务类型名
         */
        @FieldValid(fieldName = "任务类型", isNotBlank = true)
        private String typeName;



        /**
         * 任务负责人
         */
        @ExcelProperty(value = "*任务负责人", index = 2)
        @FieldValid(fieldName = "任务负责人", isNotBlank = true)
        private String chargeName;



        /**
         * 阶段名称
         */
        @ExcelProperty(value = "*阶段名称", index = 4)
        @FieldValid(fieldName = "阶段名称", isNotBlank = true)
        private String phaseName;

        /**
         * 前置任务
         */
        @ExcelProperty(value = "前置任务", index = 5)
        private String preTaskName;


        /**
         * 任务优先级 1 低级 2 中级 3 高级
         */
        private Integer priority;

        /**
         * 任务优先级
         */
        @ExcelProperty(value = "任务优先级", index = 6)
        private String priorityName;


        /**
         * 设置里程碑 0 否 1 是
         */
        private Integer isMilepost;

        /**
         * 设置里程碑
         */
        @ExcelProperty(value = "设置里程碑", index = 7)
        private String isMilepostStr;

        /**
         * 计划开始时间
         */
        @ExcelProperty(value = "计划开始时间", index = 8)
        @FieldValid(fieldName = "计划开始时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
        private LocalDate planStartTime;

        /**
         * 计划结束时间
         */
        @ExcelProperty(value = "计划结束时间", index = 9)
        @FieldValid(fieldName = "计划结束时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
        private LocalDate planEndTime;

        /**
         * 工期
         */
        @ExcelProperty(value = "工期", index = 10)
        @FieldValid(fieldName = "工期",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
        private Integer workPeriod;

        /**
         * 目标交付文档
         */
        @ExcelProperty(value = "目标交付文档", index = 11)
        private String docsName;

        /**
         * 任务描述
         */
        @ExcelProperty(value = "任务描述", index = 12)
        private String description;



    }
}
