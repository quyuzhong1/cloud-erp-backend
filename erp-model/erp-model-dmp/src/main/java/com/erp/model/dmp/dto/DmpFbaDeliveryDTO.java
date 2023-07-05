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
 * FBA发货单请求响应实体
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
*/
@Data
@NoArgsConstructor
public class DmpFbaDeliveryDTO implements Serializable {




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
        * 物流方式名称
        */
        private String logicName;

        /**
        * 清关时间
        */
        private String customsClearanceTime;

        /**
        * 总发货量
        */
        private Integer totalApplyQuantity;

        /**
        * 备注
        */
        private String remark;

        /**
        * 发货时间
        */
        private String deliveryTime;

        /**
        * 完结标识  1：完结 2：未完结
        */
        private Integer isOver;

        /**
        * 物流单价
        */
        private BigDecimal logicPrice;

        /**
        * 预计到港时间
        */
        private String estimateTime;

        /**
        * 发货单类型 1:手动发货,2:转wms发货单
        */
        private Integer deliveryType;

        /**
        * 发货单id
        */
        private String deliveryId;

        /**
        * 单个发货单的商品数
        */
        private Integer stockSum;

        /**
        * 总重量
        */
        private BigDecimal totalWeights;

        /**
        * 渠道名称
        */
        private String channelName;

        /**
        * 开船时间
        */
        private String sailTime;

        /**
        * 到货时间
        */
        private String arrivalTime;

        /**
        * 总体积
        */
        private BigDecimal totalVolumes;

        /**
        * 用户名称
        */
        private String employeeName;

        /**
        * 物流中心编码
        */
        private String logisticsCode;

        /**
        * 发货单号
        */
        private String deliveryNo;

        /**
        * 总费用
        */
        private BigDecimal extendFee;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 渠道id
        */
        private String channelId;

        /**
        * 发货单状态
        */
        private Integer deliveryStatus;

        /**
        * fba仓库id
        */
        private String warehouseId;


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
        * 物流方式名称
        */
        @NotBlank(message = "物流方式名称不能为空")
        @Size(max = 64,message = "物流方式名称最大长度不能超过64位")
        private String logicName;

        /**
        * 清关时间
        */
        @NotBlank(message = "清关时间不能为空")
        @Size(max = 32,message = "清关时间最大长度不能超过32位")
        private String customsClearanceTime;

        /**
        * 总发货量
        */
        @NotNull(message = "总发货量不能为空")
        private Integer totalApplyQuantity;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 发货时间
        */
        @NotBlank(message = "发货时间不能为空")
        @Size(max = 32,message = "发货时间最大长度不能超过32位")
        private String deliveryTime;

        /**
        * 完结标识  1：完结 2：未完结
        */
        @NotNull(message = "完结标识  1：完结 2：未完结不能为空")
        private Integer isOver;

        /**
        * 物流单价
        */
        @NotNull(message = "物流单价不能为空")
        @Digits(integer = 15, fraction = 4, message = "物流单价整数位不能超过15位，小数位不能超过4位")
        private BigDecimal logicPrice;

        /**
        * 预计到港时间
        */
        @NotBlank(message = "预计到港时间不能为空")
        @Size(max = 32,message = "预计到港时间最大长度不能超过32位")
        private String estimateTime;

        /**
        * 发货单类型 1:手动发货,2:转wms发货单
        */
        @NotNull(message = "发货单类型 1:手动发货,2:转wms发货单不能为空")
        private Integer deliveryType;

        /**
        * 发货单id
        */
        @NotBlank(message = "发货单id不能为空")
        @Size(max = 32,message = "发货单id最大长度不能超过32位")
        private String deliveryId;

        /**
        * 单个发货单的商品数
        */
        @NotNull(message = "单个发货单的商品数不能为空")
        private Integer stockSum;

        /**
        * 总重量
        */
        @NotNull(message = "总重量不能为空")
        @Digits(integer = 15, fraction = 4, message = "总重量整数位不能超过15位，小数位不能超过4位")
        private BigDecimal totalWeights;

        /**
        * 渠道名称
        */
        @NotBlank(message = "渠道名称不能为空")
        @Size(max = 255,message = "渠道名称最大长度不能超过255位")
        private String channelName;

        /**
        * 开船时间
        */
        @NotBlank(message = "开船时间不能为空")
        @Size(max = 255,message = "开船时间最大长度不能超过255位")
        private String sailTime;

        /**
        * 到货时间
        */
        @NotBlank(message = "到货时间不能为空")
        @Size(max = 255,message = "到货时间最大长度不能超过255位")
        private String arrivalTime;

        /**
        * 总体积
        */
        @NotNull(message = "总体积不能为空")
        @Digits(integer = 15, fraction = 4, message = "总体积整数位不能超过15位，小数位不能超过4位")
        private BigDecimal totalVolumes;

        /**
        * 用户名称
        */
        @NotBlank(message = "用户名称不能为空")
        @Size(max = 255,message = "用户名称最大长度不能超过255位")
        private String employeeName;

        /**
        * 物流中心编码
        */
        @NotBlank(message = "物流中心编码不能为空")
        @Size(max = 255,message = "物流中心编码最大长度不能超过255位")
        private String logisticsCode;

        /**
        * 发货单号
        */
        @NotBlank(message = "发货单号不能为空")
        @Size(max = 100,message = "发货单号最大长度不能超过100位")
        private String deliveryNo;

        /**
        * 总费用
        */
        @NotNull(message = "总费用不能为空")
        @Digits(integer = 15, fraction = 4, message = "总费用整数位不能超过15位，小数位不能超过4位")
        private BigDecimal extendFee;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 255,message = "仓库名称最大长度不能超过255位")
        private String warehouseName;

        /**
        * 渠道id
        */
        @NotBlank(message = "渠道id不能为空")
        @Size(max = 32,message = "渠道id最大长度不能超过32位")
        private String channelId;

        /**
        * 发货单状态
        */
        @NotNull(message = "发货单状态不能为空")
        private Integer deliveryStatus;

        /**
        * fba仓库id
        */
        @NotBlank(message = "fba仓库id不能为空")
        @Size(max = 32,message = "fba仓库id最大长度不能超过32位")
        private String warehouseId;


    }


}