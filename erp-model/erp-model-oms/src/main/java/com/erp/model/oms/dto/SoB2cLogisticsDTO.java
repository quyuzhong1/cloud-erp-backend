package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * B2C销售订单物流信息表请求响应实体
 *
 * @author Will
 * @since 2023-08-18
 */
@Data
@NoArgsConstructor
public class SoB2cLogisticsDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;
        /**
         * 物流运单号
         */
        private String code;
        /**
         * 物流跟踪号
         */
        private String trackNo;


        /**
         * 物流渠道名
         */
        private String logisticsChannelName;


        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 包装辅料sku编码
         */
        private String accessoriesSkuNo;

        /**
         * 中转物流商id
         */
        private String transferLogisticsSupplierId;

        /**
         * 中转物流商名
         */
        private String transferLogisticsSupplierName;
        /**
         * 中转物流商渠道id
         */
        private String transferLogisticsChannelId;

        /**
         * 中转物流商渠道名
         */
        private String transferLogisticsChannelName;

        /**
         * 第三方平台订单包裹号
         */
        private String platformPackageId;
        /**
         * 申报组织ID（sys_accounting_company.id）
         */
        private String declareOrgId;
    }

    @Data
    @NoArgsConstructor
    public static class SelectChannelDTO {

        /**
         * 销售订单id
         */
        @NotBlank(message = "销售订单不能为空")
        private String id;

        /**
         * 渠道id
         */
        @NotBlank(message = "物流渠道不能为空")
        private String logisticsChannelId;


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

        @Size(max = 32, message = "物流单号最大长度不能超过32位")
        private String code;
        /**
         * 物流跟踪号
         */
        private String trackNo;

        /**
         * 买家自选物流名称
         */
        @Size(max = 100, message = "买家自选物流名称最大长度不能超过100位")
        private String name;

        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 物流渠道 来源 http://172.16.100.11:3002/project/128/interface/api/25999
         */
        @Size(max = 32, message = "物流方式最大长度不能超过32位")
        private String logisticsChannelId;

        /**
         * 预估运费
         */
        @Digits(integer = 12, fraction = 4, message = "预估运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal estimatedShippingCost;

        /**
         * 预估运费币别
         */
        @Size(max = 32, message = "预估运费币别最大长度不能超过32位")
        private String estimatedShippingCurrency;

        /**
         * 实际运费
         */
        @Digits(integer = 12, fraction = 4, message = "实际运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualShippingCost;

        /**
         * 实际运费币别
         */
        @Size(max = 32, message = "实际运费币别最大长度不能超过32位")
        private String actualShippingCurrency;

        /**
         * 包装重量
         */
        @Digits(integer = 12, fraction = 4, message = "包装重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal weight;

        /**
         * 包装辅料skuId http://172.16.100.11:3002/project/47/interface/api/19600
         */
        @Size(max = 19, message = "包装辅料skuId最大长度不能超过19位")
        private String accessoriesSkuId;

        /**
         * 包装辅料数量
         */
        private Integer accessoriesQty;

        /**
         * 包装辅料净重
         */
        @Digits(integer = 12, fraction = 4, message = "包装辅料净重整数位不能超过12位，小数位不能超过4位")
        private BigDecimal accessoriesNw;

        /**
         * 包装辅料费
         */
        @Digits(integer = 12, fraction = 4, message = "包装辅料费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal accessoriesCost;

        /**
         * 包装辅料费币别
         */
        @Size(max = 32, message = "包装辅料费币别最大长度不能超过32位")
        private String accessoriesCostCurrency;

        /**
         * 长
         */
        @Digits(integer = 12, fraction = 4, message = "长整数位不能超过12位，小数位不能超过4位")
        private BigDecimal length;

        /**
         * 宽
         */
        @Digits(integer = 12, fraction = 4, message = "宽整数位不能超过12位，小数位不能超过4位")
        private BigDecimal width;

        /**
         * 高
         */
        @Digits(integer = 12, fraction = 4, message = "高整数位不能超过12位，小数位不能超过4位")
        private BigDecimal height;
        /**
         * ioss税号
         */
        private String iossTaxNo;
        /**
         * 申报组织ID（sys_accounting_company.id）
         */
        private String declareOrgId;
        /**
         * 申报组织名称
         */
        private String declareOrgName;
        /**
         * 统一社会信用代码
         */
        private String usciCode;
    }


    @Data
    @NoArgsConstructor
    public static class TrackNoDTO {
        /**
         * 这个是b2c 物流id
         */
        private String id;
        //渠道id
        private String logisticsChannelId;

        //b2c销售订单id
        private String soB2cId;
        private String shopId;

        private String transportNo;

        private String shopToken;

        private String soCode;

        private String platformCode;

        private String deliveryNo;

        private Integer version;
    }


    @Data
    @NoArgsConstructor
    public static class transferOrderDTO {
        @NotBlank(message = "销售订单id不能为空")
        private String id;
        /**
         * 订单号
         */
        private String code;

        @NotBlank(message = "渠道id不能为空")
        private String channelId;
        /**
         * 物流id
         */
        private String logisticsId;
        /**
         * 渠道名称
         */
        private String channelName;

        /**
         * 物流运单号
         */
        @NotBlank(message = "物流运单号不能为空")
        private String transportNo;
        /**
         * 物流跟踪号
         */
       private String trackNo;
    }
}