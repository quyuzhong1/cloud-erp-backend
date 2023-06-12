package com.erp.model.plm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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
    public static class PagingViewDTO {
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
        private List<String> planSurveyDateList;
        /**
         * 计划立项时间
         */
        private List<String> planProjectApprovalDateList;
        /**
         * 计划首批入库时间
         */
        private List<String> planFirstMassStockInDateList;
        /**
         * 计划上市时间
         */
        private List<String> planListingTimeList;
    }

    /**
     * 列表查询条件
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
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
        private String planStartDate;
        /**
         * 预计结束时间
         */
        private String planEndDate;
        /**
         * 实际完成日期
         */
        private String actualFinishDate;
    }
}
