package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 建议采购请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-29
*/
@Data
@NoArgsConstructor
public class PurchaseSuggestDTO implements Serializable {


    /**
     * 高级查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }

    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class ListParamDTO {

        /**
         * 来源id
         */
        private String sourceId;
    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {


        /**
         * 主键id
         */
        private String  id;

        /**
         * 平台类型
         */
        private String platformType;
        /**
         * 平台类型名称
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
         * 店铺名称
         */
        private String shopName;

        /**
         * 编码
         */
        private String code;

        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编码
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * sku图片
         */
        private String imagesUrl;

        /**
         * 创建类型（auto系统，manual人工）
         */
        private String dataType;
        /**
         * 创建类型名称
         */
        private String dataTypeName;

        /**
         * 状态
         */
        private String status;

        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 建议采购量
         */
        private Integer suggestPurchaseQty;

        /**
         * 计划采购量（计划修正值）
         */
        private Integer planPurchaseQty;

        /**
         * 采购备货量
         */
        private Integer purchaseStockUpQty;

        /**
         * 建议采购日期
         */
        private LocalDate suggestPurchaseDate;

        /**
         * 建议采购日期（系统）
         */
        private LocalDate sysSuggestPurchaseDate;

        /**
         * 物流方式
         */
        private String logisticsMethod;

        /**
         * 物流方式（系统）
         */
        private String sysLogisticsMethod;

        /**
         * 物流方式名称
         */
        private String logisticsMethodName;

        /**
         * 物流方式名称（系统）
         */
        private String sysLogisticsMethodName;

        /**
         * 物流时效（天）
         */
        private Integer logisticsDays;

        /**
         * 物流时效（天）（系统）
         */
        private Integer sysLogisticsDays;

        /**
         * 预计入库日期
         */
        private LocalDate estimateInstockDate;

        /**
         * 预计入库日期（系统）
         */
        private LocalDate sysEstimateInstockDate;

        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;

        /**
         * 预计可售日期（系统）
         */
        private LocalDate sysEstimateSalesDate;

        /**
         * 采购成本
         */
        private BigDecimal purchaseCost;

        /**
         * 采购成本（系统）
         */
        private BigDecimal sysPurchaseCost;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;
        /**
         * 备注
         */
        private String remark;
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
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 作废原因
         */
        private String invalidRemark;
        /**
         * 作废时间
         */
        private LocalDateTime invalidTime;
        /**
         * 作废人id
         */
        private String invalidUserId;
        /**
         * 作废人名称
         */
        private String invalidUserName;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 编码
        */
        private String code;

        /**
        * 创建类型（auto系统，manual人工）
        */
        private String dataType;

        /**
        * 建议采购量
        */
        private Integer suggestPurchaseQty;

        /**
        * 建议采购日期
        */
        private LocalDate suggestPurchaseDate;

        /**
        * 物流方式
        */
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        private Integer logisticsDays;

        /**
        * 预计入库日期
        */
        private LocalDate estimateInstockDate;

        /**
        * 预计可售日期
        */
        private LocalDate estimateSalesDate;

        /**
        * 采购成本
        */
        private BigDecimal purchaseCost;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO  {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 计划采购量（计划修正值）
         */
        private Integer planPurchaseQty;

        /**
         * 采购备货量
         */
        private Integer purchaseStockUpQty;

        /**
         * 物流方式
         */
        private String logisticsMethod;

        /**
         * 物流时效（天）
         */
        private Integer logisticsDays;

        /**
         * 建议采购日期
         */
        private LocalDate suggestPurchaseDate;

        /**
         * 预计入库日期
         */
        private LocalDate estimateInstockDate;
        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;

        /**
         * 备注
         */
        @Size(max = 100,message = "备注最大长度不能超过100位")
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 创建类型（auto系统，manual人工）
        */
        private String dataType;

        /**
        * 建议采购量
        */
        @NotNull(message = "建议采购量不能为空")
        private Integer suggestPurchaseQty;

        /**
        * 建议采购日期
        */
        @NotNull(message = "建议采购日期不能为空")
        private LocalDate suggestPurchaseDate;

        /**
        * 物流方式
        */
        @NotBlank(message = "物流方式不能为空")
        @Size(max = 64,message = "物流方式最大长度不能超过64位")
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        @NotNull(message = "物流时效（天）不能为空")
        private Integer logisticsDays;

        /**
        * 预计入库日期
        */
        private LocalDate estimateInstockDate;

        /**
        * 预计可售日期
        */
        private LocalDate estimateSalesDate;

        /**
        * 采购成本
        */
        @Digits(integer = 12, fraction = 4, message = "采购成本整数位不能超过12位，小数位不能超过4位")
        private BigDecimal purchaseCost;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源类型，replenishmentSuggestion补货建议
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;


    }
    /**
     * 导入修改
     */
    @Data
    @NoArgsConstructor
    public static class ImportUpdateDTO  {
        /**
         * id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 计划采购量（计划修正值）
         */
        @NotNull(message = "计划修正值不能为空")
        private Integer planPurchaseQty;

        /**
         * 采购备货数
         */
        @NotNull(message = "采购备货数不能为空")
        private Integer purchaseStockUpQty;

        /**
         * 备注
         */
        private String remark;
    }

}