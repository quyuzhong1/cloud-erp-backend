package com.erp.model.dmp.dto;

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
 * 中台销售订单详情表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
*/
@Data
@NoArgsConstructor
public class DmpSoDetailDTO implements Serializable {




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
        * 订单主表id
        */
        private String mainId;

        /**
        * 来源详情id
        */
        private String thirdDetailId;

        /**
        * 平台原始详情id
        */
        private String platformDetailId;

        /**
        * 产品id
        */
        private String skuId;

        /**
        * 产品编码
        */
        private String skuNo;

        /**
        * 平台sku
        */
        private String platformSku;

        /**
        * 平台产品id
        */
        private String platformSpuNo;

        /**
        * 产品多属性
        */
        private String specifics;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 单位
        */
        private String productUnit;

        /**
        * 产品名称
        */
        private String skuName;

        /**
        * 产品图片
        */
        private String skuUrl;

        /**
        * 是否赠品：true/false
        */
        private Boolean isGift;

        /**
        * 仓库编码
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 仓位
        */
        private String warehouseLocation;

        /**
        * 订单商品备注
        */
        private String itemRemark;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 商品原始售价(折扣前单价)
        */
        private BigDecimal sellPriceOrigin;

        /**
        * 折扣金额
        */
        private BigDecimal discountAmount;

        /**
        * 商品售价(折扣后单价)
        */
        private BigDecimal sellPrice;

        /**
        * 折扣后订单总金额
        */
        private BigDecimal afterAmount;

        /**
        * 运费
        */
        private BigDecimal shippingCost;

        /**
        * 拓展字段
        */
        private String extendData;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


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
        * 订单主表id
        */
        @NotBlank(message = "订单主表id不能为空")
        @Size(max = 19,message = "订单主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 来源详情id
        */
        @NotBlank(message = "来源详情id不能为空")
        @Size(max = 64,message = "来源详情id最大长度不能超过64位")
        private String thirdDetailId;

        /**
        * 平台原始详情id
        */
        @NotBlank(message = "平台原始详情id不能为空")
        @Size(max = 64,message = "平台原始详情id最大长度不能超过64位")
        private String platformDetailId;

        /**
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19,message = "产品id最大长度不能超过19位")
        private String skuId;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 64,message = "平台sku最大长度不能超过64位")
        private String platformSku;

        /**
        * 平台产品id
        */
        @NotBlank(message = "平台产品id不能为空")
        @Size(max = 64,message = "平台产品id最大长度不能超过64位")
        private String platformSpuNo;

        /**
        * 产品多属性
        */
        @NotBlank(message = "产品多属性不能为空")
        @Size(max = 255,message = "产品多属性最大长度不能超过255位")
        private String specifics;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 单位
        */
        @NotBlank(message = "单位不能为空")
        @Size(max = 25,message = "单位最大长度不能超过25位")
        private String productUnit;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 255,message = "产品名称最大长度不能超过255位")
        private String skuName;

        /**
        * 产品图片
        */
        @NotBlank(message = "产品图片不能为空")
        @Size(max = 255,message = "产品图片最大长度不能超过255位")
        private String skuUrl;

        /**
        * 是否赠品：true/false
        */
        @NotNull(message = "是否赠品：true/false不能为空")
        private Boolean isGift;

        /**
        * 仓库编码
        */
        @NotBlank(message = "仓库编码不能为空")
        @Size(max = 32,message = "仓库编码最大长度不能超过32位")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 64,message = "仓库名称最大长度不能超过64位")
        private String warehouseName;

        /**
        * 仓位
        */
        @NotBlank(message = "仓位不能为空")
        @Size(max = 32,message = "仓位最大长度不能超过32位")
        private String warehouseLocation;

        /**
        * 订单商品备注
        */
        @NotBlank(message = "订单商品备注不能为空")
        @Size(max = 500,message = "订单商品备注最大长度不能超过500位")
        private String itemRemark;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 商品原始售价(折扣前单价)
        */
        @NotNull(message = "商品原始售价(折扣前单价)不能为空")
        @Digits(integer = 12, fraction = 4, message = "商品原始售价(折扣前单价)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal sellPriceOrigin;

        /**
        * 折扣金额
        */
        @NotNull(message = "折扣金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "折扣金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal discountAmount;

        /**
        * 商品售价(折扣后单价)
        */
        @NotNull(message = "商品售价(折扣后单价)不能为空")
        @Digits(integer = 12, fraction = 4, message = "商品售价(折扣后单价)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal sellPrice;

        /**
        * 折扣后订单总金额
        */
        @NotNull(message = "折扣后订单总金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "折扣后订单总金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal afterAmount;

        /**
        * 运费
        */
        @NotNull(message = "运费不能为空")
        @Digits(integer = 12, fraction = 4, message = "运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingCost;

        /**
        * 拓展字段
        */
        private String extendData;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


    }


}