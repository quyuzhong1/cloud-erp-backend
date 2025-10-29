package com.erp.model.plm.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

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
public class AssetPurchaseChangeDetailDTO implements Serializable {




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
        * 资产采购变更单单头id
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
         * 标识(首套模、复制模) MoldInfoTagEnum
         */
        private String tag;

        /**
         * 标识(首套模、复制模) MoldInfoTagEnum
         */
        private String tagName;

        /**
        * 原采购数量
        */
        private BigDecimal oldPurchaseQty;

        /**
        * 原含税单价
        */
        private BigDecimal oldTaxPrice;

        /**
        * 原价税合计
        */
        private BigDecimal oldTotalAmount;

        /**
        * 新采购数量
        */
        private BigDecimal purchaseQty;

        /**
        * 新含税单价
        */
        private BigDecimal taxPrice;

        /**
        * 新价税合计
        */
        private BigDecimal totalAmount;

        /**
        * 币种
        */
        private String currency;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 原税率
        */
        private String oldTaxRate;

        /**
         * 税率
         */
        private String taxRate;

        /**
        * 备注
        */
        private String remark;

        /**
        * 金蝶明细id
        */
        private String kingdeeDetailId;

        /**
        * 来源明细id
        */
        private String sourceDetailId;


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
        @NotBlank(message = "id不能为空")
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 资产采购变更单单头id
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
        * 原采购数量
        */
        @Digits(integer = 12, fraction = 4, message = "原采购数量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal oldPurchaseQty;

        /**
        * 原含税单价
        */
        @Digits(integer = 12, fraction = 4, message = "原含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal oldTaxPrice;

        /**
        * 原价税合计
        */
        @Digits(integer = 12, fraction = 4, message = "原价税合计整数位不能超过12位，小数位不能超过4位")
        private BigDecimal oldTotalAmount;

        /**
         * 原税率
         */
        @NotBlank(message = "税率不能为空")
        private String oldTaxRate;

        /**
        * 新采购数量
        */
        @Digits(integer = 12, fraction = 4, message = "新采购数量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal purchaseQty;

        /**
        * 新含税单价
        */
        @Digits(integer = 12, fraction = 4, message = "新含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxPrice;

        /**
        * 新价税合计
        */
        @Digits(integer = 12, fraction = 4, message = "新价税合计整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalAmount;

        /**
         * 税率
         */
        @NotBlank(message = "税率不能为空")
        private String taxRate;

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
        * 备注
        */
        private String remark;

        /**
        * 金蝶明细id
        */
        private String kingdeeDetailId;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        private String sourceDetailId;


    }


}