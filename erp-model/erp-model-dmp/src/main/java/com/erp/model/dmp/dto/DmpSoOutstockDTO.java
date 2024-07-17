package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * 中台销售订单出库详情请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-26
*/
@Data
@NoArgsConstructor
public class DmpSoOutstockDTO implements Serializable {




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
        * 来源平台：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 单据编号（唯一）
        */
        private String thirdCode;

        /**
        * 平台原始单号
        */
        private String platformCode;

        /**
        * 状态 1.已发货 2.已作废
        */
        private String status;

        /**
        * 平台原始状态
        */
        private String platformStatus;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 物流单号
        */
        private String logisticsCode;

        /**
        * 店铺编码
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 国家二字码
        */
        private String country;

        /**
        * 买家城市
        */
        private String city;

        /**
        * 买家省份
        */
        private String province;

        /**
        * 买家地址1
        */
        private String manStreet;

        /**
        * 买家地址2
        */
        private String secondStreet;

        /**
        * 所属区域
        */
        private String district;

        /**
        * 币种
        */
        private String currencyCode;

        /**
        * 单据总金额
        */
        private BigDecimal allAmount;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 运费
        */
        private BigDecimal shippingCost;

        /**
        * 补贴金额
        */
        private BigDecimal subsidyAmount;


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
        * 来源平台：gyy，kingdee，mabang
        */
        @NotBlank(message = "来源平台：gyy，kingdee，mabang不能为空")
        @Size(max = 32,message = "来源平台：gyy，kingdee，mabang最大长度不能超过32位")
        private String sourceSystem;

        /**
        * 单据编号（唯一）
        */
        @NotBlank(message = "单据编号（唯一）不能为空")
        @Size(max = 64,message = "单据编号（唯一）最大长度不能超过64位")
        private String thirdCode;

        /**
        * 平台原始单号
        */
        @NotBlank(message = "平台原始单号不能为空")
        @Size(max = 64,message = "平台原始单号最大长度不能超过64位")
        private String platformCode;

        /**
        * 状态 1.已发货 2.已作废
        */
        @NotBlank(message = "状态 1.已发货 2.已作废不能为空")
        @Size(max = 32,message = "状态 1.已发货 2.已作废最大长度不能超过32位")
        private String status;

        /**
        * 平台原始状态
        */
        @NotBlank(message = "平台原始状态不能为空")
        @Size(max = 32,message = "平台原始状态最大长度不能超过32位")
        private String platformStatus;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 物流单号
        */
        @NotBlank(message = "物流单号不能为空")
        @Size(max = 64,message = "物流单号最大长度不能超过64位")
        private String logisticsCode;

        /**
        * 店铺编码
        */
        @NotBlank(message = "店铺编码不能为空")
        @Size(max = 32,message = "店铺编码最大长度不能超过32位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 64,message = "店铺名称最大长度不能超过64位")
        private String shopName;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 10,message = "国家二字码最大长度不能超过10位")
        private String country;

        /**
        * 买家城市
        */
        @NotBlank(message = "买家城市不能为空")
        @Size(max = 64,message = "买家城市最大长度不能超过64位")
        private String city;

        /**
        * 买家省份
        */
        @NotBlank(message = "买家省份不能为空")
        @Size(max = 64,message = "买家省份最大长度不能超过64位")
        private String province;

        /**
        * 买家地址1
        */
        @NotBlank(message = "买家地址1不能为空")
        @Size(max = 255,message = "买家地址1最大长度不能超过255位")
        private String manStreet;

        /**
        * 买家地址2
        */
        @NotBlank(message = "买家地址2不能为空")
        @Size(max = 255,message = "买家地址2最大长度不能超过255位")
        private String secondStreet;

        /**
        * 所属区域
        */
        @NotBlank(message = "所属区域不能为空")
        @Size(max = 255,message = "所属区域最大长度不能超过255位")
        private String district;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 255,message = "币种最大长度不能超过255位")
        private String currencyCode;

        /**
        * 单据总金额
        */
        @NotNull(message = "单据总金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "单据总金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal allAmount;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 运费
        */
        @NotNull(message = "运费不能为空")
        @Digits(integer = 12, fraction = 4, message = "运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingCost;

        /**
        * 补贴金额
        */
        @NotNull(message = "补贴金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "补贴金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal subsidyAmount;


    }


}