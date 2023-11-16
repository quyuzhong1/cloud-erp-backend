package com.erp.model.wms.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 海外仓入库单请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class OverseasWarehouseInboundDTO implements Serializable {




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
        * 平台类型: goodcang=谷仓，iml=艾姆勒
        */
        private String dictPlatform;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源ID
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 入库类型
        */
        private String instockType;

        /**
        * 入库状态
        */
        private String instockStatus;

        /**
        * 发货仓名称
        */
        private String deliveryWarehouseName;

        /**
        * 发货仓ID
        */
        private String deliveryWarehouseId;

        /**
        * 中转仓名称
        */
        private String transferWarehouseName;

        /**
        * 中转仓ID
        */
        private String transferWarehouseId;

        /**
        * 目的仓名称
        */
        private String toWarehouseName;

        /**
        * 目的仓ID
        */
        private String toWarehouseId;

        /**
        * 物流方式
        */
        private String logisticsMethod;

        /**
        * 备注
        */
        private String remark;

        /**
        * 最新签收时间
        */
        private LocalDateTime receiveTime;

        /**
        * 预计到达时间
        */
        private LocalDateTime estimatedArrivalDate;

        /**
        * 手动完结原因
        */
        private String finishReason;

        /**
        * 完结状态: not=未完结, auto=自动完结，manual=手动完结
        */
        private String finishStatus;

        /**
        * 第三方唯一编码
        */
        private String overseasWarehouseInboundId;


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
        * 平台类型: goodcang=谷仓，iml=艾姆勒
        */
        @NotBlank(message = "平台类型: goodcang=谷仓，iml=艾姆勒不能为空")
        @Size(max = 30,message = "平台类型: goodcang=谷仓，iml=艾姆勒最大长度不能超过30位")
        private String dictPlatform;

        /**
        * 来源单号
        */
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 50,message = "来源单号最大长度不能超过50位")
        private String sourceCode;

        /**
        * 来源ID
        */
        @NotBlank(message = "来源ID不能为空")
        @Size(max = 19,message = "来源ID最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 30,message = "来源类型最大长度不能超过30位")
        private String sourceType;

        /**
        * 入库类型
        */
        @NotBlank(message = "入库类型不能为空")
        @Size(max = 30,message = "入库类型最大长度不能超过30位")
        private String instockType;

        /**
        * 入库状态
        */
        @NotBlank(message = "入库状态不能为空")
        @Size(max = 64,message = "入库状态最大长度不能超过64位")
        private String instockStatus;

        /**
        * 发货仓名称
        */
        @NotBlank(message = "发货仓名称不能为空")
        @Size(max = 255,message = "发货仓名称最大长度不能超过255位")
        private String deliveryWarehouseName;

        /**
        * 发货仓ID
        */
        @NotBlank(message = "发货仓ID不能为空")
        @Size(max = 19,message = "发货仓ID最大长度不能超过19位")
        private String deliveryWarehouseId;

        /**
        * 中转仓名称
        */
        @NotBlank(message = "中转仓名称不能为空")
        @Size(max = 255,message = "中转仓名称最大长度不能超过255位")
        private String transferWarehouseName;

        /**
        * 中转仓ID
        */
        @NotBlank(message = "中转仓ID不能为空")
        @Size(max = 19,message = "中转仓ID最大长度不能超过19位")
        private String transferWarehouseId;

        /**
        * 目的仓名称
        */
        @NotBlank(message = "目的仓名称不能为空")
        @Size(max = 255,message = "目的仓名称最大长度不能超过255位")
        private String toWarehouseName;

        /**
        * 目的仓ID
        */
        @NotBlank(message = "目的仓ID不能为空")
        @Size(max = 19,message = "目的仓ID最大长度不能超过19位")
        private String toWarehouseId;

        /**
        * 物流方式
        */
        @NotBlank(message = "物流方式不能为空")
        @Size(max = 64,message = "物流方式最大长度不能超过64位")
        private String logisticsMethod;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 最新签收时间
        */
        private LocalDateTime receiveTime;

        /**
        * 预计到达时间
        */
        private LocalDateTime estimatedArrivalDate;

        /**
        * 手动完结原因
        */
        @NotBlank(message = "手动完结原因不能为空")
        @Size(max = 255,message = "手动完结原因最大长度不能超过255位")
        private String finishReason;

        /**
        * 完结状态: not=未完结, auto=自动完结，manual=手动完结
        */
        @NotBlank(message = "完结状态: not=未完结, auto=自动完结，manual=手动完结不能为空")
        @Size(max = 64,message = "完结状态: not=未完结, auto=自动完结，manual=手动完结最大长度不能超过64位")
        private String finishStatus;

        /**
        * 第三方唯一编码
        */
        @NotBlank(message = "第三方唯一编码不能为空")
        @Size(max = 255,message = "第三方唯一编码最大长度不能超过255位")
        private String overseasWarehouseInboundId;


    }


}