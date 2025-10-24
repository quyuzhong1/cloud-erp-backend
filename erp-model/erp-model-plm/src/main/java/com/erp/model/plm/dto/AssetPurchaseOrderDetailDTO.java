package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.*;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
*/
@Data
@NoArgsConstructor
public class AssetPurchaseOrderDetailDTO implements Serializable {


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
        * 资产采购单单头id
        */
        private String mainId;

        /**
        * 资产id
        */
        private String assetId;

        /**
        * 资产编码
        */
        private String assetCode;

        /**
        * 资产名称
        */
        private String assetName;

        /**
         * 标识(首套模first、复制模copy)
         */
        private String tag;

        /**
         * 标识名称(首套模first、复制模copy)
         */
        private String tagName;

        /**
        * 含税单价
        */
        private BigDecimal taxPrice;

        /**
        * 币种
        */
        private String currency;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 采购数量
        */
        private BigDecimal purchaseQty;

        /**
        * 价税合计
        */
        private BigDecimal totalAmount;

        /**
        * 计划交期
        */
        private LocalDate planDeliveryDate;

        /**
        * 是否加急
        */
        private Boolean isUrgent;

        /**
        * 备注
        */
        private String remark;

        /**
        * 金蝶明细id
        */
        private String kingdeeDetailId;

        /**
        * 结束收货AssetPurchaseOrderReceiveEnum
        */
        private String endReceive;

        /**
         * 结束收货名称AssetPurchaseOrderReceiveEnum
         */
        private String endReceiveName;

        /**
        * 结束验收时间
        */
        private LocalDateTime endReceiveTime;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
         * 关联SKU详情
         */
        private List<AssetPurchaseOrderDetailDTO.AssetDetailRefSkuDTO> assetDetailRefSkuDTOList;
    }

    /**
     * 关联SKU详情
     */
    @Data
    @NoArgsConstructor
    public static class AssetDetailRefSkuDTO {

        /**
         * 资产id
         */
        private String assetId;

        /**
         * 资产编码
         */
        private String assetCode;

        /**
         * 资产名称
         */
        private String assetName;

        /**
         * 项目编号
         */
        private String projectCode;

        /**
         * 项目名称
         */
        private String projectName;
        /**
         * skuId
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 用量
         */
        private BigDecimal skuQty;
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
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 资产采购单单头id
        */
        private String mainId;

        /**
        * 资产id
        */
        @NotBlank(message = "资产id不能为空")
        private String assetId;

        /**
        * 资产编码
        */
        @NotBlank(message = "资产编码不能为空")
        private String assetCode;

        /**
        * 资产名称
        */
        @NotBlank(message = "资产名称不能为空")
        private String assetName;

        /**
        * 含税单价
        */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        @DecimalMin(value = "0.0", inclusive = false, message = "单价必须大于0")
        private BigDecimal taxPrice;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
        * 币种符号
        */
        @NotBlank(message = "币种符号不能为空")
        private String currencySymbol;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
        * 采购数量
        */
        @NotNull(message = "采购数量不能为空")
        @Digits(integer = 12, fraction = 4, message = "采购数量整数位不能超过12位，小数位不能超过4位")
        @DecimalMin(value = "0.0", inclusive = false, message = "采购数量必须大于0")
        private BigDecimal purchaseQty;

        /**
        * 价税合计
        */
        @Digits(integer = 12, fraction = 4, message = "价税合计整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalAmount;

        /**
        * 计划交期
        */
        @NotNull(message = "计划交期不能为空")
        private LocalDate planDeliveryDate;

        /**
        * 是否加急
        */
        @NotNull(message = "是否加急不能为空")
        private Boolean isUrgent;

        /**
        * 备注
        */
        private String remark;

        /**
        * 金蝶明细id
        */
        private String kingdeeDetailId;

        /**
        * 结束收货AssetPurchaseOrderReceiveEnum
        */
        private String endReceive;

        /**
        * 结束验收时间
        */
        private LocalDateTime endReceiveTime;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
         * 标识
         */
        @NotNull(message = "标识不能为空")
        private String tag;

    }


}