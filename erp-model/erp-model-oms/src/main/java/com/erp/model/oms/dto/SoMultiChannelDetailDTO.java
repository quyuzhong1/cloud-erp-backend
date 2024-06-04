package com.erp.model.oms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 多渠道订单明细表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-05-30
*/
@Data
@NoArgsConstructor
public class SoMultiChannelDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 图片URL
        */
        private String imageUrl;

        /**
        * skuId
        */
        private String skuId;

        /**
        * 产品sku编号
        */
        private String skuNo;

        /**
        * 平台sku
        */
        private String platformSkuNo;

        /**
        * 平台产品id
        */
        private String platformSpuNo;

        /**
        * 库存sku编号
        */
        private String warehouseSkuNo;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 单价
        */
        private BigDecimal price;

        /**
        * 金额
        */
        private BigDecimal amount;

        /**
        * 币别（原币）
        */
        private String currency;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 建议售价（本位币）
        */
        private BigDecimal advicePrice;

        /**
        * 含税成本（本位币）
        */
        private BigDecimal taxCost;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 标签json
        */
        private String labelJson;

        /**
        * 库存组织id
        */
        private String warehouseOrgId;

        /**
        * 库存组织名称
        */
        private String warehouseOrgName;

        /**
        * 库位
        */
        private String warehouseLocation;

        /**
        * 平台明细行号
        */
        private String platformLineNumber;

        /**
        * 来源平台，字典soB2cSourcePlatform
        */
        private String sourcePlatform;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 图片URL
        */
        @NotBlank(message = "图片URL不能为空")
        private String imageUrl;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 100,message = "平台sku最大长度不能超过100位")
        private String platformSkuNo;

        /**
        * 平台产品id
        */
        @NotBlank(message = "平台产品id不能为空")
        @Size(max = 100,message = "平台产品id最大长度不能超过100位")
        private String platformSpuNo;

        /**
        * 库存sku编号
        */
        @NotBlank(message = "库存sku编号不能为空")
        @Size(max = 100,message = "库存sku编号最大长度不能超过100位")
        private String warehouseSkuNo;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 200,message = "仓库名称最大长度不能超过200位")
        private String warehouseName;

        /**
        * 单价
        */
        @NotNull(message = "单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal price;

        /**
        * 金额
        */
        @NotNull(message = "金额不能为空")
        private BigDecimal amount;

        /**
        * 币别（原币）
        */
        @NotBlank(message = "币别（原币）不能为空")
        @Size(max = 32,message = "币别（原币）最大长度不能超过32位")
        private String currency;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 10, fraction = 6, message = "汇率整数位不能超过10位，小数位不能超过6位")
        private BigDecimal exchangeRate;

        /**
        * 建议售价（本位币）
        */
        @NotNull(message = "建议售价（本位币）不能为空")
        @Digits(integer = 12, fraction = 4, message = "建议售价（本位币）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal advicePrice;

        /**
        * 含税成本（本位币）
        */
        @NotNull(message = "含税成本（本位币）不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税成本（本位币）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxCost;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 500,message = "来源明细id最大长度不能超过500位")
        private String sourceDetailId;

        /**
        * 标签json
        */
        @NotBlank(message = "标签json不能为空")
        private String labelJson;

        /**
        * 库存组织id
        */
        @NotBlank(message = "库存组织id不能为空")
        @Size(max = 19,message = "库存组织id最大长度不能超过19位")
        private String warehouseOrgId;

        /**
        * 库存组织名称
        */
        @NotBlank(message = "库存组织名称不能为空")
        @Size(max = 50,message = "库存组织名称最大长度不能超过50位")
        private String warehouseOrgName;

        /**
        * 库位
        */
        @NotBlank(message = "库位不能为空")
        @Size(max = 32,message = "库位最大长度不能超过32位")
        private String warehouseLocation;

        /**
        * 平台明细行号
        */
        @NotBlank(message = "平台明细行号不能为空")
        @Size(max = 64,message = "平台明细行号最大长度不能超过64位")
        private String platformLineNumber;

        /**
        * 来源平台，字典soB2cSourcePlatform
        */
        @NotBlank(message = "来源平台，字典soB2cSourcePlatform不能为空")
        @Size(max = 32,message = "来源平台，字典soB2cSourcePlatform最大长度不能超过32位")
        private String sourcePlatform;


    }


}