package com.erp.model.fms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.*;

/**
 * <p>
 * 资产处置单资产明细表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-29
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
        * 来源ID
        */
        private String sourceId;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源明细ID
        */
        private String sourceDetailId;

        /**
        * 主表ID
        */
        private String mainId;

        /**
        * 资产名称
        */
        private String assetName;

        /**
        * 单位 Pcs
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
        private String disposalCurrencyName;

        /**
        * 清理费用
        */
        private BigDecimal cleanupCost;

        /**
        * 残值收入 含税
        */
        private BigDecimal residualValue;

        /**
        * 发票类型：ordinary=普通发票, addedValue增值发票
        */
        private String invoiceType;
        private String invoiceTypeName;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 税额
        */
        private BigDecimal taxAmount;

        private List<AssetDisposalPhysicalDetailDTO.ViewDTO> assetDisposalPhysicalDetailDTOList;

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
        private String id;


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 来源ID
        */
        @NotBlank(message = "来源ID不能为空")
        private String sourceId;

        /**
        * 来源单号
        */
        @NotBlank(message = "来源单号不能为空")
        private String sourceCode;

        /**
        * 来源明细ID
        */
        @NotBlank(message = "来源明细ID不能为空")
        private String sourceDetailId;

        /**
        * 主表ID
        */
        private String mainId;

        /**
        * 资产名称
        */
        private String assetName;

        /**
        * 单位 Pcs
        */
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
//        @NotBlank(message = "处置币类不能为空")
        private String disposalCurrency;

        /**
        * 清理费用
        */
//        @NotNull(message = "清理费用不能为空")
        @Digits(integer = 13, fraction = 2, message = "清理费用整数位不能超过13位，小数位不能超过2位")
        private BigDecimal cleanupCost;

        /**
        * 残值收入 含税
        */
//        @NotNull(message = "残值收入 含税不能为空")
        @Digits(integer = 13, fraction = 2, message = "残值收入 含税整数位不能超过13位，小数位不能超过2位")
        private BigDecimal residualValue;

        /**
        * 发票类型：ordinary=普通发票, addedValue增值发票
        */
//        @NotBlank(message = "发票类型：ordinary=普通发票, addedValue增值发票不能为空")
        private String invoiceType;

        /**
        * 税率
        */
//        @NotNull(message = "税率不能为空")
        @DecimalMin(value = "0",inclusive = false, message = "税率不能小于0")
        @DecimalMax(value = "100",inclusive = true, message = "税率不能大于100")
        private BigDecimal taxRate;

        /**
        * 税额
        */
//        @NotNull(message = "税额不能为空")
        @Digits(integer = 13, fraction = 2, message = "税额整数位不能超过13位，小数位不能超过2位")
        private BigDecimal taxAmount;


        @NotEmpty(message = "资产实物明细不能为空")
        private List<AssetDisposalPhysicalDetailDTO.UpdateDTO> assetDisposalPhysicalDetailDTOList;

    }


}