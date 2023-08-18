package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.entity.StocktakingProfitLossDetailEntity;
import com.erp.model.wms.enums.BillTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname StocktakingProfitLossDTO
 * @Description
 * @Date 2023-08-03 18:07
 * @Created by yl
 */
public class StocktakingProfitLossDTO implements Serializable {


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
    public static class PagingParamDTO extends SortDTO {
        /**
         * 单号
         */
        private String code;

        /**
         * 来源单号 盘点任务单号
         */
        private String sourceCode;

        /**
         * skuId
         */
        private List<String> skuIdList;


        /**
         * 单据类型
         */
        private List<String> billTypeList;

        /**
         * 盘点人
         */
        private String stocktakingUserId;


        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 创建人id
         */
        private List<String> createUserIdList;

        /**
         * 审核时间
         */
        private List<LocalDate> approveTimeList;

        /**
         * 审核人
         */
        private List<String> approveUserIdList;

        /**
         * 仓库
         */
        private String warehouseId;

        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 标识
         */
        @StateEnumValue(strValues = {"all", "profit", "loss"}, message = "tab类型有误")
        @NotBlank(message = "tab不能为空")
        private String tabFlag;

    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        private List<String> ids;

    }


    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {


        private String id;
        /**
         * 单号
         */
        private String code;

        /**
         * 来源单号 盘点任务单号
         */
        private String sourceCode;

        /**
         * 来源id 盘点任务id
         */
        private String sourceId;

        /**
         * 单据类型
         */
        private BillTypeEnum billType;

        /**
         * 单据类型名
         */
        private String billTypeName;

        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;


        /**
         * 盘点人
         */
        private String stocktakingUserName;
        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 最新审核人
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime approveTime;


        List<StocktakingProfitLossDetailDTO.ViewDTO> detailList;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        private String id;
        /**
         * 单号
         */
        private String code;

        /**
         * 来源单号 盘点任务单号
         */
        private String sourceCode;

        /**
         * 来源id 盘点任务id
         */
        private String sourceId;

        /**
         * 单据类型
         */
        private BillTypeEnum billType;

        /**
         * 单据类型名
         */
        private String billTypeName;

        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态名
         */
        private String approveStatusName;


        /**
         * 盘点人
         */
        private String stocktakingUserName;
        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;


        /**
         * 审核时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime approveTime;


        List<StocktakingProfitLossDetailDTO.ViewDTO> detailList;

    }
}
