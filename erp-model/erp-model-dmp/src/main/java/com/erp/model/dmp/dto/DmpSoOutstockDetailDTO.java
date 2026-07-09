package com.erp.model.dmp.dto;

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
 * 中台销售订单出库详情明细表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-04-11
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

        /**
        * 备注
        */
        private String remark;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 来源订单明细id
        */
        private String srcOrderDetailId;

        /**
        * 客户名称
        */
        private String customerName;

        /**
        * 平台名称
        */
        private String platformName;

        /**
        * 销售部门名称
        */
        private String saleDeptName;

        /**
        * 销售员名称
        */
        private String salesManName;

        /**
        * 价税合计(本位币)-旺店通用
        */
        private BigDecimal allAmountLocalCurrency;

        /**
        * 币别
        */
        private String currency;

        /**
        * 支付单价
        */
        private BigDecimal payAmount;

        /**
        * 支付币别
        */
        private String payCurrency;

        /**
        * 折扣单价
        */
        private BigDecimal discountAmount;

        /**
        * 折扣币别
        */
        private String discountCurrency;

        /**
        * 明细状态
        */
        private String dataStatus;

        /**
        * 平台类型
        */
        private String platformType;

        /**
        * 是否组合品
        */
        private Integer isComb;

        /**
        * 组合装编码
        */
        private String suiteNo;

        /**
        * 组合装名称
        */
        private String suiteName;

        /**
         * 单据编号（唯一）
         */
        private String thirdCode;
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
        @Size(max = 500,message = "来源详情id最大长度不能超过500位")
        private String thirdDetailId;

        /**
        * 销售平台原始详情id
        */
        @NotBlank(message = "销售平台原始详情id不能为空")
        @Size(max = 500,message = "销售平台原始详情id最大长度不能超过500位")
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
        @Digits(integer = 18, fraction = 6, message = "商品单价整数位不能超过18位，小数位不能超过6位")
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
        @Digits(integer = 18, fraction = 6, message = "金额整数位不能超过18位，小数位不能超过6位")
        private BigDecimal amount;

        /**
        * 第三方平台订单编号
        */
        @NotBlank(message = "第三方平台订单编号不能为空")
        @Size(max = 500,message = "第三方平台订单编号最大长度不能超过500位")
        private String thirdOrderCode;

        /**
        * 销售平台原始订单编号
        */
        @NotBlank(message = "销售平台原始订单编号不能为空")
        @Size(max = 500,message = "销售平台原始订单编号最大长度不能超过500位")
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

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 18, fraction = 6, message = "税率整数位不能超过18位，小数位不能超过6位")
        private BigDecimal taxRate;

        /**
        * 来源订单明细id
        */
        @NotBlank(message = "来源订单明细id不能为空")
        @Size(max = 100,message = "来源订单明细id最大长度不能超过100位")
        private String srcOrderDetailId;

        /**
        * 客户名称
        */
        @NotBlank(message = "客户名称不能为空")
        @Size(max = 100,message = "客户名称最大长度不能超过100位")
        private String customerName;

        /**
        * 平台名称
        */
        @NotBlank(message = "平台名称不能为空")
        @Size(max = 100,message = "平台名称最大长度不能超过100位")
        private String platformName;

        /**
        * 销售部门名称
        */
        @NotBlank(message = "销售部门名称不能为空")
        @Size(max = 100,message = "销售部门名称最大长度不能超过100位")
        private String saleDeptName;

        /**
        * 销售员名称
        */
        @NotBlank(message = "销售员名称不能为空")
        @Size(max = 100,message = "销售员名称最大长度不能超过100位")
        private String salesManName;

        /**
        * 价税合计(本位币)-旺店通用
        */
        @NotNull(message = "价税合计(本位币)不能为空")
        @Digits(integer = 18, fraction = 6, message = "价税合计(本位币)整数位不能超过18位，小数位不能超过6位")
        private BigDecimal allAmountLocalCurrency;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 255,message = "币别最大长度不能超过255位")
        private String currency;

        /**
        * 支付单价
        */
        @NotNull(message = "支付单价不能为空")
        @Digits(integer = 18, fraction = 6, message = "支付单价整数位不能超过18位，小数位不能超过6位")
        private BigDecimal payAmount;

        /**
        * 支付币别
        */
        @NotBlank(message = "支付币别不能为空")
        @Size(max = 255,message = "支付币别最大长度不能超过255位")
        private String payCurrency;

        /**
        * 折扣单价
        */
        @NotNull(message = "折扣单价不能为空")
        @Digits(integer = 18, fraction = 6, message = "折扣单价整数位不能超过18位，小数位不能超过6位")
        private BigDecimal discountAmount;

        /**
        * 折扣币别
        */
        @NotBlank(message = "折扣币别不能为空")
        @Size(max = 255,message = "折扣币别最大长度不能超过255位")
        private String discountCurrency;

        /**
        * 明细状态
        */
        @NotBlank(message = "明细状态不能为空")
        @Size(max = 64,message = "明细状态最大长度不能超过64位")
        private String detailStatus;

        /**
        * 平台类型
        */
        @NotBlank(message = "平台类型不能为空")
        @Size(max = 64,message = "平台类型最大长度不能超过64位")
        private String platformType;

        /**
        * 是否组合品
        */
        @NotNull(message = "是否组合品不能为空")
        private Integer isComb;

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


    }


}