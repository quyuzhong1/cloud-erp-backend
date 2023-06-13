package com.erp.model.plm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 项目报表
 * @Author Luo_WG
 * @Date 2023/6/12 19:11
 **/
public class ProjectReportFormsDTO implements Serializable {

    /**
     * 分页查询
     */
    @Data
    @NoArgsConstructor
    public static class PagingParam {
        /**
         * id
         */
        private List<String> ids;
        /**
         * 项目状态
         */
        private String projectStatus;
        /**
         * spu/sku/产品名称
         */
        private String searchKeyword;
        /**
         * 产品经理
         */
        private List<String> productChargeIdList;
        /**
         * 产品分类
         */
        private String categoryId;
        /**
         * 计划调研时间
         */
        private List<LocalDate> planSurveyDateList;
        /**
         * 计划立项时间
         */
        private List<LocalDate> planProjectApprovalDateList;
        /**
         * 计划首批入库时间
         */
        private List<LocalDate> planFirstMassStockInDateList;
        /**
         * 计划上市时间
         */
        private List<LocalDate> planListingTimeList;
    }

    /**
     * 列表查询条件
     */
    @Data
    @NoArgsConstructor
    public static class PagingView extends SortDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * SKU编号
         */
        private String skuNo;
        /**
         * SPU编号
         */
        private String spuNo;
        /**
         * 二级分类
         */
        private String category;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 产品等级
         */
        private String grade;
        /**
         * 项目属性
         */
        private String property;
        /**
         * 产品品牌
         */
        private String brandName;
        /**
         * 产品经理
         */
        private String chargeName;
        /**
         * 项目经理
         */
        private String projectChargeName;
        /**
         * 立项状态
         */
        private String approvalStatus;
        /**
         * 项目状态
         */
        private String projectStatus;
        /**
         * 项目阶段
         */
        private String projectPhase;
        /**
         * 立项进度
         */
        private String approvalProgress;
        /**
         * 项目进度
         */
        private String projectProgress;
        /**
         * 项目进展状态名
         */
        private String progressStatusName;
        /**
         * 总任务数
         */
        private String totalTaskCount;
        /**
         * 已完成任务数
         */
        private String finishedCount;
        /**
         * 未完成任务数
         */
        private String unfinishedCount;
        /**
         * 本周任务数
         */
        private String thisWeekTaskCount;
        /**
         * 本周已完成任务数
         */
        private String thisWeekFinishedCount;
        /**
         * 本周未完成任务数
         */
        private String thisWeekUnfinishedCount;
        /**
         * 逾期已完成任务数
         */
        private String delayFinishedCount;
        /**
         * 逾期未完成任务数
         */
        private String delayUnfinishedCount;
        /**
         * 预计开始时间
         */
        private LocalDate planStartDate;
        /**
         * 预计结束时间
         */
        private LocalDate planEndDate;
        /**
         * 实际完成日期
         */
        private LocalDate actualFinishDate;
    }

    /**
     * 任务详情列表查询条件
     */
    @Data
    @NoArgsConstructor
    public static class TaskDetailParam {
        /**
         * id
         */
        private String id;
        /**
         * 任务名称
         */
        private String taskName;
        /**
         * 任务状态
         */
        private String taskState;
        /**
         * 本周开始日期
         */
        private LocalDate startDate;
        /**
         * 本周结束日期
         */
        private LocalDate endDate;
    }

    /**
     * 任务详情列表
     */
    @Data
    @NoArgsConstructor
    public static class taskDetail {
        /**
         * 阶段名称
         */
        private String phaseName;
        /**
         * 任务名称
         */
        private String taskName;
        /**
         * 任务负责人
         */
        private String chargeName;
        /**
         * 计划开始日期
         */
        private LocalDate planStartDate;
        /**
         * 计划结束日期
         */
        private LocalDate planEndDate;
        /**
         * 实际开始日期
         */
        private LocalDate realityStartDate;
        /**
         * 实际结束日期
         */
        private LocalDate realityEndDate;
        /**
         * 任务状态
         */
        private String taskState;
    }

}
