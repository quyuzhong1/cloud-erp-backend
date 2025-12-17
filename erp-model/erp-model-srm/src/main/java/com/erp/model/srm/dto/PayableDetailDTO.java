package com.erp.model.srm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-09-24
*/
@Data
@NoArgsConstructor
public class PayableDetailDTO implements Serializable {




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
        * skuId
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 含税单价
        */
        private BigDecimal taxIncludedPrice;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 折扣率
        */
        private BigDecimal discountRate;

        /**
        * 预付金额
        */
        private BigDecimal prepayAmount;

        /**
        * 价税合计（折扣后）
        */
        private BigDecimal discountTaxAmount;

        /**
        * 价税合计
        */
        private BigDecimal taxIncludedTotal;

        /**
        * 币别
        */
        private String currency;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 采购订单明细id
        */
        private String poDetailId;

        /**
        * 采购订单id
        */
        private String poId;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 业务单id,入库单id/退货单id
        */
        private String businessId;

        /**
        * 业务单编码，入库单编码/退货单编码
        */
        private String businessCode;

        /**
        * 业务单据类型,入库单/退货单
        */
        private String businessType;

        /**
        * 业务单明细id
        */
        private String businessDetailId;

        /**
        * 三方系统明细id
        */
        private String thirdPayableDetailId;

        /**
        * 备注
        */
        private String remark;


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
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 含税单价
        */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxIncludedPrice;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
        * 折扣率
        */
        @NotNull(message = "折扣率不能为空")
        @Digits(integer = 12, fraction = 4, message = "折扣率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal discountRate;

        /**
        * 预付金额
        */
        @NotNull(message = "预付金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "预付金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal prepayAmount;

        /**
        * 价税合计（折扣后）
        */
        @NotNull(message = "价税合计（折扣后）不能为空")
        @Digits(integer = 12, fraction = 4, message = "价税合计（折扣后）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal discountTaxAmount;

        /**
        * 价税合计
        */
        @NotNull(message = "价税合计不能为空")
        @Digits(integer = 12, fraction = 4, message = "价税合计整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxIncludedTotal;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 采购订单明细id
        */
        @NotBlank(message = "采购订单明细id不能为空")
        @Size(max = 19,message = "采购订单明细id最大长度不能超过19位")
        private String poDetailId;

        /**
        * 采购订单id
        */
        @NotBlank(message = "采购订单id不能为空")
        @Size(max = 19,message = "采购订单id最大长度不能超过19位")
        private String poId;

        /**
         * 采购订单编码
         */
        private String poCode;

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        private String mainId;

        /**
        * 业务单id,入库单id/退货单id
        */
        @NotBlank(message = "业务单id,入库单id/退货单id不能为空")
        @Size(max = 19,message = "业务单id,入库单id/退货单id最大长度不能超过19位")
        private String businessId;

        /**
        * 业务单编码，入库单编码/退货单编码
        */
        @NotBlank(message = "业务单编码，入库单编码/退货单编码不能为空")
        @Size(max = 32,message = "业务单编码，入库单编码/退货单编码最大长度不能超过32位")
        private String businessCode;

        /**
        * 业务单据类型,入库单/退货单
        */
        @NotBlank(message = "业务单据类型,入库单/退货单不能为空")
        @Size(max = 32,message = "业务单据类型,入库单/退货单最大长度不能超过32位")
        private String businessType;

        /**
        * 业务单明细id
        */
        @NotBlank(message = "业务单明细id不能为空")
        @Size(max = 19,message = "业务单明细id最大长度不能超过19位")
        private String businessDetailId;

        /**
        * 三方系统明细id
        */
        @NotBlank(message = "三方系统明细id不能为空")
        @Size(max = 19,message = "三方系统明细id最大长度不能超过19位")
        private String thirdPayableDetailId;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}