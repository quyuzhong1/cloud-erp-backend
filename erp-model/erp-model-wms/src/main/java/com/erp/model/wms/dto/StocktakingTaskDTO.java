package com.erp.model.wms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 盘点任务
 *
 * @author Lambda
 * @Classname StocktakingDTO
 * @Description TODO
 * @Date 2023-07-31 14:29
 * @Created by yl
 */
public class StocktakingTaskDTO implements Serializable {

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabDTO {
        /**
         * 标识
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;

    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {
        /**
         * 盘点计划单号/盘点任务单号
         */
        private String code;

        /**
         * 单据状态集合
         */
        private List<String> approveStatusList;

        /**
         * 盘点状态集合
         */
        private List<String> stocktakingStatusList;

        /**
         * 盘点方式集合
         */
        private List<String> inventoryModeList;

        /**
         * 盘点类型集合
         */
        private List<String> inventoryTypeList;


        /**
         * 分单规则集合
         */
        private List<String> separateRuleList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 创建人id
         */
        private List<LocalDate> createUserId;

        /**
         * 仓库
         */
        private String warehouseId;
        /**
         * 标识
         */
        private String tabFlag;

    }

    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 盘点任务单号
         */
        private String code;

        /**
         * 盘点计划单号
         */
        private String sourceCode;

        /**
         * 盘点方式编码
         */
        private String inventoryMode;

        /**
         * 盘点方式名
         */
        private String inventoryModeName;

        /**
         * 盘点类型
         */
        private String inventoryType;

        /**
         * 盘点类型名
         */
        private String inventoryTypeName;

        /**
         * 单据状态编码
         */
        private String approveStatus;

        /**
         * 单据状态名
         */
        private String approveStatusName;

        /**
         * 分单规则编码
         */
        private String separateRule;

        /**
         * 分单规则编码名
         */
        private String separateRuleName;

        /**
         * 盘点状态
         */
        private String inventoryStatus;


        /**
         * 盘点状态名
         */
        private String inventoryStatusName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * sku 统计数
         */
        private String skuCount;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 盘点人
         */
        private String inventoryUserName;


    }
}
