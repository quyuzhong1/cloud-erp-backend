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
 * 中台销售订单出库详情明细表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-26
*/
@Data
@NoArgsConstructor
public class DmpSoOutstockDetailDTO implements Serializable {




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
        * 来源详情id
        */
        private String thirdDetailId;

        /**
        * 销售平台原始详情id
        */
        private String platformDetailId;

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
        private String skuName;

        /**
        * 平台sku
        */
        private String platformSku;

        /**
        * 单位
        */
        private String productUnit;

        /**
        * 是否赠品：true/false
        */
        private Boolean isGift;

        /**
        * 商品规格
        */
        private String specifics;

        /**
        * 商品备注
        */
        private String itemRemark;

        /**
        * 仓库编号
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 商品仓位
        */
        private String warehouseLocation;

        /**
        * 商品单价
        */
        private BigDecimal sellPrice;

        /**
        * 商品数量
        */
        private Integer qty;

        /**
        * 金额
        */
        private BigDecimal amount;

        /**
        * 第三方平台订单编号
        */
        private String thirdOrderCode;

        /**
        * 销售平台原始订单编号
        */
        private String platformOrderCode;

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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 来源详情id
        */
        @NotBlank(message = "来源详情id不能为空")
        @Size(max = 64,message = "来源详情id最大长度不能超过64位")
        private String thirdDetailId;

        /**
        * 销售平台原始详情id
        */
        @NotBlank(message = "销售平台原始详情id不能为空")
        @Size(max = 64,message = "销售平台原始详情id最大长度不能超过64位")
        private String platformDetailId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 500,message = "产品名称最大长度不能超过500位")
        private String skuName;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 64,message = "平台sku最大长度不能超过64位")
        private String platformSku;

        /**
        * 单位
        */
        @NotBlank(message = "单位不能为空")
        @Size(max = 16,message = "单位最大长度不能超过16位")
        private String productUnit;

        /**
        * 是否赠品：true/false
        */
        @NotNull(message = "是否赠品：true/false不能为空")
        private Boolean isGift;

        /**
        * 商品规格
        */
        @NotBlank(message = "商品规格不能为空")
        @Size(max = 255,message = "商品规格最大长度不能超过255位")
        private String specifics;

        /**
        * 商品备注
        */
        @NotBlank(message = "商品备注不能为空")
        @Size(max = 500,message = "商品备注最大长度不能超过500位")
        private String itemRemark;

        /**
        * 仓库编号
        */
        @NotBlank(message = "仓库编号不能为空")
        @Size(max = 32,message = "仓库编号最大长度不能超过32位")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 64,message = "仓库名称最大长度不能超过64位")
        private String warehouseName;

        /**
        * 商品仓位
        */
        @NotBlank(message = "商品仓位不能为空")
        @Size(max = 32,message = "商品仓位最大长度不能超过32位")
        private String warehouseLocation;

        /**
        * 商品单价
        */
        @NotNull(message = "商品单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "商品单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal sellPrice;

        /**
        * 商品数量
        */
        @NotNull(message = "商品数量不能为空")
        private Integer qty;

        /**
        * 金额
        */
        @NotNull(message = "金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal amount;

        /**
        * 第三方平台订单编号
        */
        @NotBlank(message = "第三方平台订单编号不能为空")
        @Size(max = 64,message = "第三方平台订单编号最大长度不能超过64位")
        private String thirdOrderCode;

        /**
        * 销售平台原始订单编号
        */
        @NotBlank(message = "销售平台原始订单编号不能为空")
        @Size(max = 64,message = "销售平台原始订单编号最大长度不能超过64位")
        private String platformOrderCode;

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