package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
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
    public static class PagingParam extends SortDTO {
        /**
         * id
         */
        private List<String> ids;
        /**
         * 列表头项目状态 plm/common/enumDropDown?type=ProjectReportStatus
         * 状态描述：notApproval：未立项  approval：已立项  finished：已完成
         */
        private String approvalStatus;
        /**
         * 项目状态（前端不用传，后端自用状态转换）
         */
        private List<Integer> approvalStatusList;
        /**
         * 立项状态（前端不用传，后端自用状态转换）
         */
        private List<Integer> projectStatusList;
        /**
         * spu/sku/产品名称
         */
        private String searchKeyword;
        /**
         * 产品等级
         */
        private List<String> gradeIdList;
        /**
         * 产品属性id
         */
        private List<String> propertyIdList;
        /**
         * 项目经理id
         */
        private List<String> projectChargeIdList;
        /**
         * 产品等级
         */
        private String grade;
        /**
         * 产品经理
         */
        private List<String> productChargeIdList;
        /**
         * 产品经理（后端用）
         */
        private String chargeIdSplit;
        /**
         * 产品分类
         */
        private List<String> categoryIdList;
        /**
         * 立项状态
         */
        private List<Integer> searchApprovalStatusList;
        /**
         * 项目状态
         */
        private List<Integer> searchProjectStatusList;

    }

    /**
     * 列表查询条件
     */
    @Data
    public static class PagingView extends SortDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 产品图片
         */
        private String imageUrl;

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
         * 项目经理id
         */
        private String projectChargeId;

        /**
         * 项目经理
         */
        private String projectChargeName;

        /**
         * 立项状态
         */
        private Integer approvalStatus;

        /**
         * 立项状态名称
         */
        private String approvalStatusName;

        /**
         * 项目状态
         */
        private Integer projectStatus;

        /**
         * 项目状态名称
         */
        private String projectStatusName;

        /**
         * 项目阶段
         */
        private String projectPhase;

        /**
         * 立项任务完成数量
         */
        private Integer approvalFinishTaskCount;

        /**
         * 立项任务总数量
         */
        private Integer approvalTaskCount;

        /**
         * 项目任务完成数量
         */
        private Integer projectFinishTaskCount;

        /**
         * 项目任务总数量
         */
        private Integer projectTaskCount;

        /**
         * 立项进度
         */
        private BigDecimal approvalProgress;

        /**
         * 项目进度
         */
        private BigDecimal projectProgress;

        /**
         * 项目进展状态
         */
        private String progressStatus;

        /**
         * 项目进展状态名称
         */
        private String progressStatusName;

        /**
         * 总任务数
         */
        private Integer totalTaskCount;

        /**
         * 已完成任务数
         */
        private Integer finishedCount;

        /**
         * 未完成任务数
         */
        private Integer unfinishedCount;

        /**
         * 本周任务数
         */
        private Integer thisWeekTaskCount;

        /**
         * 本周已完成任务数
         */
        private Integer thisWeekFinishedCount;

        /**
         * 本周未完成任务数
         */
        private Integer thisWeekUnfinishedCount;

        /**
         * 逾期已完成任务数
         */
        private Integer delayFinishedCount;

        /**
         * 逾期未完成任务数
         */
        private Integer delayUnfinishedCount;

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

        public PagingView() {
            this.approvalFinishTaskCount = 0;
            this.approvalTaskCount = 0;
            this.projectFinishTaskCount = 0;
            this.projectTaskCount = 0;
            this.totalTaskCount = 0;
            this.finishedCount = 0;
            this.unfinishedCount = 0;
            this.thisWeekTaskCount = 0;
            this.thisWeekFinishedCount = 0;
            this.thisWeekUnfinishedCount = 0;
            this.delayFinishedCount = 0;
            this.delayUnfinishedCount = 0;
        }
    }

    /**
     * 任务详情列表查询条件
     */
    @Data
    @NoArgsConstructor
    public static class TaskDetailParam {
        /**
         * ids
         */
        private List<String> ids;
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
        private List<LocalDate> thisWeekDateList;
    }

    /**
     * 任务详情列表
     */
    @Data
    @NoArgsConstructor
    public static class TaskDetail {
        /**
         * 任务id
         */
        private String id;
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
        private Integer taskState;
        /**
         * 任务状态名称
         */
        private String taskStateName;
    }

}
