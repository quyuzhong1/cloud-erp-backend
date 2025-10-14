package com.erp.model.fms.dto;

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
 * 资产处置单资产明细表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetDisposalDetailDTO implements Serializable {




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
        * 来源明细ID
        */
        private String sourceDetailId;

        /**
        * 主表ID
        */
        private String mainId;

        /**
        * 卡片ID
        */
        private String cardId;

        /**
        * 卡片明细ID
        */
        private String cardDetailId;

        /**
        * 卡片编码
        */
        private String cardCode;

        /**
        * 资产名称
        */
        private String assetName;

        /**
        * 单位 PCS
        */
        private String unit;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 处置数量
        */
        private Integer disposalQty;

        /**
        * 处置币类
        */
        private String disposalCurrency;

        /**
        * 清理费用
        */
        private BigDecimal cleanupCost;

        /**
        * 残值收入 含税
        */
        private BigDecimal residualValue;

        /**
        * 发票类型（普通发票、增值发票）
        */
        private String invoiceType;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 税额
        */
        private BigDecimal taxAmount;


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
        * 来源明细ID
        */
        @NotBlank(message = "来源明细ID不能为空")
        @Size(max = 19,message = "来源明细ID最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 主表ID
        */
        @NotBlank(message = "主表ID不能为空")
        @Size(max = 19,message = "主表ID最大长度不能超过19位")
        private String mainId;

        /**
        * 卡片ID
        */
        @NotBlank(message = "卡片ID不能为空")
        @Size(max = 19,message = "卡片ID最大长度不能超过19位")
        private String cardId;

        /**
        * 卡片明细ID
        */
        @NotBlank(message = "卡片明细ID不能为空")
        @Size(max = 19,message = "卡片明细ID最大长度不能超过19位")
        private String cardDetailId;

        /**
        * 卡片编码
        */
        @NotBlank(message = "卡片编码不能为空")
        @Size(max = 50,message = "卡片编码最大长度不能超过50位")
        private String cardCode;

        /**
        * 资产名称
        */
        @NotBlank(message = "资产名称不能为空")
        @Size(max = 200,message = "资产名称最大长度不能超过200位")
        private String assetName;

        /**
        * 单位 PCS
        */
        @NotBlank(message = "单位 PCS不能为空")
        @Size(max = 20,message = "单位 PCS最大长度不能超过20位")
        private String unit;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 处置数量
        */
        @NotNull(message = "处置数量不能为空")
        private Integer disposalQty;

        /**
        * 处置币类
        */
        @NotBlank(message = "处置币类不能为空")
        @Size(max = 10,message = "处置币类最大长度不能超过10位")
        private String disposalCurrency;

        /**
        * 清理费用
        */
        @NotNull(message = "清理费用不能为空")
        @Digits(integer = 13, fraction = 2, message = "清理费用整数位不能超过13位，小数位不能超过2位")
        private BigDecimal cleanupCost;

        /**
        * 残值收入 含税
        */
        @NotNull(message = "残值收入 含税不能为空")
        @Digits(integer = 13, fraction = 2, message = "残值收入 含税整数位不能超过13位，小数位不能超过2位")
        private BigDecimal residualValue;

        /**
        * 发票类型（普通发票、增值发票）
        */
        @NotBlank(message = "发票类型（普通发票、增值发票）不能为空")
        @Size(max = 20,message = "发票类型（普通发票、增值发票）最大长度不能超过20位")
        private String invoiceType;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 3, fraction = 2, message = "税率整数位不能超过3位，小数位不能超过2位")
        private BigDecimal taxRate;

        /**
        * 税额
        */
        @NotNull(message = "税额不能为空")
        @Digits(integer = 13, fraction = 2, message = "税额整数位不能超过13位，小数位不能超过2位")
        private BigDecimal taxAmount;


    }


}