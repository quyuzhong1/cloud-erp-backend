package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * b2c报关对账单明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-19
*/
@Data
@NoArgsConstructor
public class TmsB2cDeclareReconciliationDetailDTO implements Serializable {




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
        * 来源id
        */
        private String sourceId;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 销售订单明细id
        */
        private String soDetailId;

        /**
        * 销售订单编码
        */
        private String soCode;

        /**
        * 入库预报日期
        */
        private LocalDate date;

        /**
        * 国家
        */
        private String country;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道名称
        */
        private String logisticsChannelName;

        /**
        * 产品数量
        */
        private Integer qty;

        /**
        * 预估重量
        */
        private BigDecimal estimateWeight;

        /**
        * 预估重量单位
        */
        private String estimateWeightUnit;

        /**
        * 实际重量
        */
        private BigDecimal actualWeight;

        /**
        * 实际重量单位
        */
        private String actualWeightUnit;

        /**
        * 实际计费重
        */
        private BigDecimal actualBillingWeight;

        /**
        * 实际物流运费
        */
        private BigDecimal actualShippingCost;

        /**
        * 实际报关费
        */
        private BigDecimal actualDeclareCost;

        /**
        * 实际其他费
        */
        private BigDecimal actualOtherCost;

        /**
        * 备注
        */
        private String remark;

        /**
        * 对账状态
        */
        private String status;

        /**
        * 确认时间
        */
        private LocalDate confirmDate;

        /**
        * 确认人id
        */
        private String confirmUserId;

        /**
        * 确认人名称
        */
        private String confirmUserName;


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
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 来源编码
        */
        @NotBlank(message = "来源编码不能为空")
        @Size(max = 32,message = "来源编码最大长度不能超过32位")
        private String sourceCode;

        /**
        * 销售订单id
        */
        @NotBlank(message = "销售订单id不能为空")
        @Size(max = 19,message = "销售订单id最大长度不能超过19位")
        private String soId;

        /**
        * 销售订单明细id
        */
        @NotBlank(message = "销售订单明细id不能为空")
        @Size(max = 19,message = "销售订单明细id最大长度不能超过19位")
        private String soDetailId;

        /**
        * 销售订单编码
        */
        @NotBlank(message = "销售订单编码不能为空")
        @Size(max = 32,message = "销售订单编码最大长度不能超过32位")
        private String soCode;

        /**
        * 入库预报日期
        */
        private LocalDate date;

        /**
        * 国家
        */
        @NotBlank(message = "国家不能为空")
        @Size(max = 32,message = "国家最大长度不能超过32位")
        private String country;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 物流渠道名称
        */
        @NotBlank(message = "物流渠道名称不能为空")
        @Size(max = 100,message = "物流渠道名称最大长度不能超过100位")
        private String logisticsChannelName;

        /**
        * 产品数量
        */
        @NotNull(message = "产品数量不能为空")
        private Integer qty;

        /**
        * 预估重量
        */
        @NotNull(message = "预估重量不能为空")
        @Digits(integer = 12, fraction = 4, message = "预估重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal estimateWeight;

        /**
        * 预估重量单位
        */
        @NotBlank(message = "预估重量单位不能为空")
        @Size(max = 32,message = "预估重量单位最大长度不能超过32位")
        private String estimateWeightUnit;

        /**
        * 实际重量
        */
        @NotNull(message = "实际重量不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualWeight;

        /**
        * 实际重量单位
        */
        @NotBlank(message = "实际重量单位不能为空")
        @Size(max = 32,message = "实际重量单位最大长度不能超过32位")
        private String actualWeightUnit;

        /**
        * 实际计费重
        */
        @NotNull(message = "实际计费重不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际计费重整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualBillingWeight;

        /**
        * 实际物流运费
        */
        @NotNull(message = "实际物流运费不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际物流运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualShippingCost;

        /**
        * 实际报关费
        */
        @NotNull(message = "实际报关费不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际报关费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualDeclareCost;

        /**
        * 实际其他费
        */
        @NotNull(message = "实际其他费不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际其他费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualOtherCost;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 对账状态
        */
        @NotBlank(message = "对账状态不能为空")
        @Size(max = 32,message = "对账状态最大长度不能超过32位")
        private String status;

        /**
        * 确认时间
        */
        private LocalDate confirmDate;

        /**
        * 确认人id
        */
        @NotBlank(message = "确认人id不能为空")
        @Size(max = 19,message = "确认人id最大长度不能超过19位")
        private String confirmUserId;

        /**
        * 确认人名称
        */
        @NotBlank(message = "确认人名称不能为空")
        @Size(max = 32,message = "确认人名称最大长度不能超过32位")
        private String confirmUserName;


    }


}