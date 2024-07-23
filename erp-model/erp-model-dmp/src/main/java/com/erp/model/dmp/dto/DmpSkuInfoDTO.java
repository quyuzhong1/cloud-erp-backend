package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * 中台产品表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-07-23
*/
@Data
@NoArgsConstructor
public class DmpSkuInfoDTO implements Serializable {




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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 产品id
        */
        private String spuId;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 中文名
        */
        private String name;

        /**
        * 统一成本价
        */
        private BigDecimal defaultCost;

        /**
        * 状态：1 在售 2 赠品 3 已下架 4 卖完下架 5 未上架 6 清仓 7 定制
        */
        private String status;

        /**
        * 品牌名称
        */
        private String brandName;

        /**
        * 商品目录(一级)
        */
        private String parentCategoryName;

        /**
        * 商品目录(二级)
        */
        private String categoryName;

        /**
        * 售价
        */
        private BigDecimal sellPrice;

        /**
        * 申报价格
        */
        private BigDecimal declarePrice;

        /**
        * 负责人id
        */
        private String chargeId;

        /**
        * 负责人名称
        */
        private String chargeName;

        /**
        * 企业id
        */
        private String companyId;

        /**
        * 企业名称
        */
        private String companyName;

        /**
        * 物料属性 1.外购 2.自制 3.委外 4.服务
        */
        private String prodcutProperty;

        /**
        * sku重量
        */
        private BigDecimal grossWeight;

        /**
        * sku物流尺寸-高
        */
        private BigDecimal packageHeight;

        /**
        * sku物流尺寸-宽
        */
        private BigDecimal packageWidth;

        /**
        * sku物流尺寸-长
        */
        private BigDecimal packageLength;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 图片url
        */
        private String imageUrls;

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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 64,message = "产品id最大长度不能超过64位")
        private String spuId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 64,message = "skuId最大长度不能超过64位")
        private String skuId;

        /**
        * 中文名
        */
        @NotBlank(message = "中文名不能为空")
        @Size(max = 500,message = "中文名最大长度不能超过500位")
        private String name;

        /**
        * 统一成本价
        */
        @NotNull(message = "统一成本价不能为空")
        @Digits(integer = 12, fraction = 4, message = "统一成本价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal defaultCost;

        /**
        * 状态：1 在售 2 赠品 3 已下架 4 卖完下架 5 未上架 6 清仓 7 定制
        */
        @NotBlank(message = "状态：1 在售 2 赠品 3 已下架 4 卖完下架 5 未上架 6 清仓 7 定制不能为空")
        @Size(max = 16,message = "状态：1 在售 2 赠品 3 已下架 4 卖完下架 5 未上架 6 清仓 7 定制最大长度不能超过16位")
        private String status;

        /**
        * 品牌名称
        */
        @NotBlank(message = "品牌名称不能为空")
        @Size(max = 255,message = "品牌名称最大长度不能超过255位")
        private String brandName;

        /**
        * 商品目录(一级)
        */
        @NotBlank(message = "商品目录(一级)不能为空")
        @Size(max = 255,message = "商品目录(一级)最大长度不能超过255位")
        private String parentCategoryName;

        /**
        * 商品目录(二级)
        */
        @NotBlank(message = "商品目录(二级)不能为空")
        @Size(max = 255,message = "商品目录(二级)最大长度不能超过255位")
        private String categoryName;

        /**
        * 售价
        */
        @NotNull(message = "售价不能为空")
        @Digits(integer = 12, fraction = 4, message = "售价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal sellPrice;

        /**
        * 申报价格
        */
        @NotNull(message = "申报价格不能为空")
        @Digits(integer = 14, fraction = 2, message = "申报价格整数位不能超过14位，小数位不能超过2位")
        private BigDecimal declarePrice;

        /**
        * 负责人id
        */
        @NotBlank(message = "负责人id不能为空")
        @Size(max = 64,message = "负责人id最大长度不能超过64位")
        private String chargeId;

        /**
        * 负责人名称
        */
        @NotBlank(message = "负责人名称不能为空")
        @Size(max = 64,message = "负责人名称最大长度不能超过64位")
        private String chargeName;

        /**
        * 企业id
        */
        @NotBlank(message = "企业id不能为空")
        @Size(max = 64,message = "企业id最大长度不能超过64位")
        private String companyId;

        /**
        * 企业名称
        */
        @NotBlank(message = "企业名称不能为空")
        @Size(max = 255,message = "企业名称最大长度不能超过255位")
        private String companyName;

        /**
        * 物料属性 1.外购 2.自制 3.委外 4.服务
        */
        @NotBlank(message = "物料属性 1.外购 2.自制 3.委外 4.服务不能为空")
        @Size(max = 16,message = "物料属性 1.外购 2.自制 3.委外 4.服务最大长度不能超过16位")
        private String prodcutProperty;

        /**
        * sku重量
        */
        @NotNull(message = "sku重量不能为空")
        @Digits(integer = 14, fraction = 2, message = "sku重量整数位不能超过14位，小数位不能超过2位")
        private BigDecimal grossWeight;

        /**
        * sku物流尺寸-高
        */
        @NotNull(message = "sku物流尺寸不能为空")
        @Digits(integer = 14, fraction = 2, message = "sku物流尺寸整数位不能超过14位，小数位不能超过2位")
        private BigDecimal packageHeight;

        /**
        * sku物流尺寸-宽
        */
        @NotNull(message = "sku物流尺寸不能为空")
        @Digits(integer = 14, fraction = 2, message = "sku物流尺寸整数位不能超过14位，小数位不能超过2位")
        private BigDecimal packageWidth;

        /**
        * sku物流尺寸-长
        */
        @NotNull(message = "sku物流尺寸不能为空")
        @Digits(integer = 14, fraction = 2, message = "sku物流尺寸整数位不能超过14位，小数位不能超过2位")
        private BigDecimal packageLength;

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 64,message = "主表id最大长度不能超过64位")
        private String mainId;

        /**
        * 图片url
        */
        @NotBlank(message = "图片url不能为空")
        @Size(max = 500,message = "图片url最大长度不能超过500位")
        private String imageUrls;

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