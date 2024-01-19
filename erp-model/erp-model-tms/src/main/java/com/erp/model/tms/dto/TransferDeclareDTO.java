package com.erp.model.tms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * 中转报关表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class TransferDeclareDTO implements Serializable {




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
        * 单据编号
        */
        private String code;

        /**
        * 预计中转日期
        */
        private LocalDate planTransferDate;

        /**
        * 上传状态
        */
        private String uploadStatus;

        /**
        * 发货物流商id
        */
        private String deliveryLogisticsSupplierId;

        /**
        * 发货物流商中文
        */
        private String deliveryLogisticsSupplierName;

        /**
        * 中转物流商id
        */
        private String transferLogisticsSupplierId;

        /**
        * 中转物流服务商
        */
        private String transferLogisticsSupplierName;

        /**
        * 中转渠道id
        */
        private String transferChannelId;

        /**
        * 中转渠道中文
        */
        private String transferChannelName;

        /**
        * 包裹总数量
        */
        private Integer packageTotalQty;

        /**
        * 包裹总重量
        */
        private BigDecimal packageTotalWeight;

        /**
        * 包裹重量单位
        */
        private String weightUnit;


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
        * 预计中转日期
        */
        private LocalDate planTransferDate;

        /**
        * 上传状态
        */
        @NotBlank(message = "上传状态不能为空")
        @Size(max = 30,message = "上传状态最大长度不能超过30位")
        private String uploadStatus;

        /**
        * 发货物流商id
        */
        @NotBlank(message = "发货物流商id不能为空")
        @Size(max = 19,message = "发货物流商id最大长度不能超过19位")
        private String deliveryLogisticsSupplierId;

        /**
        * 发货物流商中文
        */
        @NotBlank(message = "发货物流商中文不能为空")
        @Size(max = 255,message = "发货物流商中文最大长度不能超过255位")
        private String deliveryLogisticsSupplierName;

        /**
        * 中转物流商id
        */
        @NotBlank(message = "中转物流商id不能为空")
        @Size(max = 19,message = "中转物流商id最大长度不能超过19位")
        private String transferLogisticsSupplierId;

        /**
        * 中转物流服务商
        */
        @NotBlank(message = "中转物流服务商不能为空")
        @Size(max = 255,message = "中转物流服务商最大长度不能超过255位")
        private String transferLogisticsSupplierName;

        /**
        * 中转渠道id
        */
        @NotBlank(message = "中转渠道id不能为空")
        @Size(max = 19,message = "中转渠道id最大长度不能超过19位")
        private String transferChannelId;

        /**
        * 中转渠道中文
        */
        @NotBlank(message = "中转渠道中文不能为空")
        @Size(max = 255,message = "中转渠道中文最大长度不能超过255位")
        private String transferChannelName;

        /**
        * 包裹总数量
        */
        @NotNull(message = "包裹总数量不能为空")
        private Integer packageTotalQty;

        /**
        * 包裹总重量
        */
        @NotNull(message = "包裹总重量不能为空")
        @Digits(integer = 6, fraction = 4, message = "包裹总重量整数位不能超过6位，小数位不能超过4位")
        private BigDecimal packageTotalWeight;

        /**
        * 包裹重量单位
        */
        @NotBlank(message = "包裹重量单位不能为空")
        @Size(max = 30,message = "包裹重量单位最大长度不能超过30位")
        private String weightUnit;


    }


}