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
 * 中台配货单明细表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-04-17
*/
@Data
@NoArgsConstructor
public class DmpSoDeliveryDetailDTO implements Serializable {




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
        * 第三方发货明细id
        */
        private String thirdDeliveryDetailId;

        /**
        * 第三方明细创建时间
        */
        private LocalDateTime thirdDetailCreateTime;

        /**
        * 第三方明细更新时间
        */
        private LocalDateTime thirdDetailUpdateTime;

        /**
        * 商家SKU编码
        */
        private String skuNo;

        /**
        * 商家SKU名称
        */
        private String skuName;

        /**
        * 成交数量
        */
        private Integer transactionQty;

        /**
        * 成交单价
        */
        private BigDecimal transactionPrice;

        /**
        * 单位
        */
        private String unit;

        /**
        * 成交金额
        */
        private BigDecimal transactionAmount;

        /**
        * 规格型号
        */
        private String spuNo;

        /**
        * 规格型号名称
        */
        private String spuName;

        /**
        * 是否赠品
        */
        private Integer isGift;

        /**
        * 是否组合装
        */
        private Integer isComb;

        /**
        * 是否虚拟商品
        */
        private Integer isVirtual;

        /**
        * 是否服务类商品
        */
        private Integer isService;

        /**
        * 明细状态
        */
        private String detailStatus;

        /**
        * 基准售价
        */
        private BigDecimal listPrice;

        /**
        * 交易币别代码
        */
        private String currencyCode;

        /**
        * 交易币别名称
        */
        private String currencyName;

        /**
        * 组合装编码
        */
        private String suiteNo;

        /**
        * 组合装名称
        */
        private String suiteName;

        /**
        * MSKU编码
        */
        private String platformSkuNo;

        /**
        * MSKU名称
        */
        private String platformSkuName;

        /**
        * 结算币别代码
        */
        private String settlementCurrencyCode;

        /**
        * 数据状态(已创建，已更新，已删除等)
        */
        private String dataStatus;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

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
        @Size(max = 64,message = "主表id最大长度不能超过64位")
        private String mainId;

        /**
        * 第三方发货明细id
        */
        @NotBlank(message = "第三方发货明细id不能为空")
        @Size(max = 64,message = "第三方发货明细id最大长度不能超过64位")
        private String thirdDeliveryDetailId;

        /**
        * 第三方明细创建时间
        */
        private LocalDateTime thirdDetailCreateTime;

        /**
        * 第三方明细更新时间
        */
        private LocalDateTime thirdDetailUpdateTime;

        /**
        * 商家SKU名称
        */
        @NotBlank(message = "商家SKU名称不能为空")
        @Size(max = 255,message = "商家SKU名称最大长度不能超过255位")
        private String skuName;

        /**
        * 成交数量
        */
        @NotNull(message = "成交数量不能为空")
        private Integer transactionQty;

        /**
        * 成交单价
        */
        @NotNull(message = "成交单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "成交单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal transactionPrice;

        /**
        * 单位
        */
        @NotBlank(message = "单位不能为空")
        @Size(max = 64,message = "单位最大长度不能超过64位")
        private String unit;

        /**
        * 成交金额
        */
        @NotNull(message = "成交金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "成交金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal transactionAmount;

        /**
        * 规格型号
        */
        @NotBlank(message = "规格型号不能为空")
        @Size(max = 255,message = "规格型号最大长度不能超过255位")
        private String spuNo;

        /**
        * 规格型号名称
        */
        @NotBlank(message = "规格型号名称不能为空")
        @Size(max = 255,message = "规格型号名称最大长度不能超过255位")
        private String spuName;

        /**
        * 是否赠品
        */
        @NotNull(message = "是否赠品不能为空")
        private Integer isGift;

        /**
        * 是否组合装
        */
        @NotNull(message = "是否组合装不能为空")
        private Integer isComb;

        /**
        * 是否虚拟商品
        */
        @NotNull(message = "是否虚拟商品不能为空")
        private Integer isVirtual;

        /**
        * 是否服务类商品
        */
        @NotNull(message = "是否服务类商品不能为空")
        private Integer isService;

        /**
        * 明细状态
        */
        @NotBlank(message = "明细状态不能为空")
        @Size(max = 255,message = "明细状态最大长度不能超过255位")
        private String detailStatus;

        /**
        * 基准售价
        */
        @NotNull(message = "基准售价不能为空")
        @Digits(integer = 12, fraction = 4, message = "基准售价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal listPrice;

        /**
        * 交易币别代码
        */
        @NotBlank(message = "交易币别代码不能为空")
        @Size(max = 64,message = "交易币别代码最大长度不能超过64位")
        private String currencyCode;

        /**
        * 交易币别名称
        */
        @NotBlank(message = "交易币别名称不能为空")
        @Size(max = 64,message = "交易币别名称最大长度不能超过64位")
        private String currencyName;

        /**
        * 组合装编码
        */
        @NotBlank(message = "组合装编码不能为空")
        @Size(max = 64,message = "组合装编码最大长度不能超过64位")
        private String suiteNo;

        /**
        * 组合装名称
        */
        @NotBlank(message = "组合装名称不能为空")
        @Size(max = 255,message = "组合装名称最大长度不能超过255位")
        private String suiteName;

        /**
        * MSKU编码
        */
        @NotBlank(message = "MSKU编码不能为空")
        @Size(max = 64,message = "MSKU编码最大长度不能超过64位")
        private String platformSkuNo;

        /**
        * MSKU名称
        */
        @NotBlank(message = "MSKU名称不能为空")
        @Size(max = 255,message = "MSKU名称最大长度不能超过255位")
        private String platformSkuName;

        /**
        * 结算币别代码
        */
        @NotBlank(message = "结算币别代码不能为空")
        @Size(max = 64,message = "结算币别代码最大长度不能超过64位")
        private String settlementCurrencyCode;

        /**
        * 数据状态(已创建，已更新，已删除等)
        */
        @NotBlank(message = "数据状态(已创建，已更新，已删除等)不能为空")
        @Size(max = 64,message = "数据状态(已创建，已更新，已删除等)最大长度不能超过64位")
        private String dataStatus;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

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


    }


}