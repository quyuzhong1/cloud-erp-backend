package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.mrp.enums.ReplenishmentInventoryTypeEnum;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReplenishmentSuggestionDTO implements Serializable {


    @Getter
    @Setter
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
        /**
         * 建议类型
         */
        @NotBlank(message = "建议类型不能为空")
        private String platformType;
        /**
         * sku类型
         */
        private String skuType;
        /**
         * 是否关注
         */
        private Boolean favorite;
    }


    /**
     * 暂不补货/恢复补货
     */
    @Data
    @NoArgsConstructor
    public static class ReplenishmentDTO {
        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 补货原因
         */
        @NotBlank(message = "原因说明不能为空")
        private String replenishmentRemark;
    }

    /**
     * 批量设置规则参数
     */
    @Data
    @NoArgsConstructor
    public static class UpdateRuleDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 备货设置
         */
        @Valid
        private CfgRuleStockUpDTO.UpdateDTO stockUpUpdateDTO;

        /**
         * 销量设置
         */
        @Valid
        private CfgRuleSalesQtyDTO.UpdateDetailDTO salesQtyUpdateDTO;

    }

    /**
     * 批量设置规则参数
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateRuleDTO {

        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 备货设置
         */
        @Valid
        private CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO;

        /**
         * 销量设置
         */
        @Valid
        private CfgRuleSalesQtyDTO.UpdateDetailDTO salesQtyUpdateDTO;

    }

    /**
     * 恢复规则
     */
    @Data
    @NoArgsConstructor
    public static class RestoreRuleDTO {

        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 规则选项
         */
        private List<String> ruleTypeList;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateLabelDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 标签选项
         */
        private List<String> labelIdList;

    }

    @Data
    @NoArgsConstructor
    public static class BatchSaveLabelDTO {
        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 标签选项
         */
        private List<String> labelIdList;

    }

    @Getter
    @Setter
    public static class DetailParamDTO {

        /**
         * 明细id
         */
        @NotBlank(message = "明细id不能为空")
        private String detailId;

        /**
         * 类型
         * @see ReplenishmentInventoryTypeEnum
         */
        private String type;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;
    }

    @Data
    @NoArgsConstructor
    public static class PurchaseSuggestionDTO {

        /**
         * 类型
         */
        private String platformType;
        /**
         * 类型名称
         */
        private String platformTypeName;
        /**
         * 平台
         */
        private String platform;
        /**
         * 平台名称
         */
        private String platformName;
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺
         */
        private String shopName;

        /**
         * 单号
         */
        private String code;

        /**
         * 创建类型
         */
        private String createType;

        /**
         * SKUID
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 品名
         */
        private String productName;

        /**
         * 系统建议值
         */
        private Integer suggestPurchaseQty;
        /**
         * 物流方式（系统）
         */
        private String logisticsMethod;
        /**
         * 物流方式名称（系统）
         */
        private String logisticsMethodName;

        /**
         * 建议采购日期（系统）
         */
        private LocalDate suggestPurchaseDate;

        /**
         * 预计入库日期（系统）
         */
        private LocalDate estimateInstockDate;

        /**
         * 预计可售日期（系统）
         */
        private LocalDate estimateSalesDate;

        /**
         * 预计采购成本（系统）
         */
        private BigDecimal purchaseCost;

        /**
         * 创建方式
         */
        private String createTypeName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private String updateTime;
    }

    @Data
    @NoArgsConstructor
    public static class ReplenishmentRuleExportDTO {
        /**
         * 备货导出
         */
        private List<CfgRuleStockUpDTO.StockUpExportDTO> stockUpExportList;

        /**
         * 动态备货系数导出
         */
        private List<CfgRuleStockingRatioDTO.StockingRatioExportDTO> stockingRatioExportList;

        /**
         * 默认日销量导出
         */
        private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> defaultSalesQtyExportList;

        /**
         * 动态日销量导出
         */
        private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> dynamicSalesQtyExportList;

        /**
         * 固定日销量导出
         */
        private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> fixedSalesQtyExportList;

        /**
         * 销量去噪导出
         */
        private List<CfgRuleSalesDenoisingDTO.salesDenoisingExportDTO> salesDenoisingExportList;
    }


    @Getter
    @Setter
    public static class SalesDTO {

        private String skuId;

        private String shopId;

        private String originalSalesQty;
    }
}
