package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.RequestIdTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

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
     * 列表查询入参
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 入库类型
         * /api/wms/common/enumDropDown?type=OverseasInstockType
         */
        private String instockType;

        /**
         * 交货方式
         * /api/wms/common/enumDropDown?type=OverseasDeliveryMode
         */
        private String deliveryMode;
        /**
         * 入库状态
         * /api/wms/common/enumDropDown?type=OverseasInstockStatus
         */
        private List<String> instockStatusList;

        /**
         * 发货仓ID
         */
        private List<String> deliveryWarehouseIdList;

        /**
         * 中转仓ID
         */
        private List<String> transferWarehouseIdList;

        /**
         * 目的仓ID
         */
        private List<String> toWarehouseIdList;

        /**
         * 物流方式
         * /api/wms/common/enumDropDown?type=LogisticsMethod
         */
        private List<String> logisticsMethodList;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

        /**
         * ERP SKU列表
         */
        private List<String> skuNoList;

        /**
         * 海外仓库存SKU列表
         */
        private List<String> platformSkuNoList;

        /**
         * 创建人id列表
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDateTime> createTimeList;

        /**
         * 签收时间
         */
        private List<OffsetDateTime> receiveTimeList;

        /**
         * 收发差异：/wms/common/enumDropDown?type=DiffRule
         * 描述： >:大于0，<:小于0，=:等于0，<>:不等于0
         */
        private String diffRule;

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

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
         * 入库类型名称
         */
        private String instockTypeName;

        /**
         * 入库状态
         */
        private String instockStatus;

        /**
         * 入库状态名称
         */
        private String instockStatusName;

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
         * 物流方式名称
         */
        private String logisticsMethodName;

        /**
         * 交货方式
         * /api/wms/common/enumDropDown?type=OverseasDeliveryMode
         */
        private String deliveryMode;

        /**
         * 交货方式名称
         */
        private String deliveryModeName;

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
         * 完结状态名称
         */
        private String finishStatusName;

        /**
         * 第三方唯一编码
         */
        private String overseasWarehouseInboundId;

        /**
         * 详情id
         */
        private String detailId;

        /**
         * 海外仓平台产品名称
         */
        private String platformProductName;

        /**
         * 海外仓平台SKU号
         */
        private String platformSkuNo;

        /**
         * ERP系统产品名称
         */
        private String productName;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * ERP的SKU ID
         */
        private String skuId;

        /**
         * 是否组合品：combination 组合 single 单品
         */
        private Boolean isCombination;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 在途数量
         */
        private Integer transportQty;

        /**
         * 装箱数量
         */
        private Integer packQty;

        /**
         * 收发差异
         */
        private Integer diffQty;

        /**
         * 签收时间
         */
        private LocalDateTime detailReceiveTime;

        /**
         * 签收状态：not=未签收，already=已签收
         */
        private String receiveStatus;

        /**
         * 签收类型：system=平台系统签收，manual=手动签收
         */
        private String receiveType;
    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

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
         * 入库类型名称
         */
        private String instockTypeName;

        /**
         * 入库状态
         */
        private String instockStatus;

        /**
         * 入库状态名称
         */
        private String instockStatusName;

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
         * SKU信息
         */
        private List<OverseasWarehouseInboundDetailDTO.ViewDTO> detailList;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 发货单ID
         */
        @NotBlank(message = "发货单ID不能为空")
        private String deliveryId;

        /**
         * 入库类型
         * selfHeadway=自发头程
         * transferAgent=中转代发
         */
        @NotNull(message = "入库类型不能为空")
        @JsonDeserialize(using = OverseasInstockTypeEnum.OverseasInStockTypeDeserializer.class)
        private OverseasInstockTypeEnum instockType;

        /**
         * 物流方式
         * airfreight=空运
         * express=快递
         * oceanFreightBulk=海运散装
         * oceanFreightFCL=海运整箱
         * railwayTransportationBulk=铁运散装
         * railwayTransportationFCL=铁运整箱
         */
        @NotNull(message = "物流方式不能为空")
        @JsonDeserialize(using = LogisticsMethodEnum.LogisticsMethodDeserializer.class)
        private LogisticsMethodEnum logisticsMethod;


        /**
         * 备注
         */
        private String remark;

        /**
         * 预计到达时间
         */
        @NotNull(message = "预计到达时间不能为空")
        private LocalDateTime estimatedArrivalDate;

        /**
         * 物流跟踪号
         */
        private String trackingNo;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 入库类型
         */
        @NotNull(message = "入库类型不能为空")
        @JsonDeserialize(using = OverseasInstockTypeEnum.OverseasInStockTypeDeserializer.class)
        private OverseasInstockTypeEnum instockType;

        /**
         * 物流方式
         */
        @NotNull(message = "物流方式不能为空")
        @JsonDeserialize(using = LogisticsMethodEnum.LogisticsMethodDeserializer.class)
        private LogisticsMethodEnum logisticsMethod;

        /**
         * 备注
         */
        @Size(max = 255, message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 预计到达时间
         */
        @NotNull(message = "预计到达时间不能为空")
        private LocalDateTime estimatedArrivalDate;

        /**
         * 物流跟踪号
         */
        private String trackingNo;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class ReceivedDTO {

        /**
         * 详情detailId
         */
        @NotBlank(message = "详情detailId不能为空")
        private String detailId;

        /**
         * 签收数量
         */
        @NotNull(message = "签收数量不能为空")
        private Integer receivedQty;
    }

    /**
     * 手动完结
     */
    @Data
    @NoArgsConstructor
    public static class FinishDTO {

        /**
         * 海外入库单ID
         */
        @NotBlank(message = "海外入库单ID不能为空")
        private String id;

        /**
         * 完结原因
         */
        @NotNull(message = "完结原因不能为空")
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 平台类型: goodcang=谷仓，iml=艾姆勒
         */
        @NotBlank(message = "平台类型: goodcang=谷仓，iml=艾姆勒不能为空")
        @Size(max = 30, message = "平台类型: goodcang=谷仓，iml=艾姆勒最大长度不能超过30位")
        private String dictPlatform;

        /**
         * 来源单号
         */
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 50, message = "来源单号最大长度不能超过50位")
        private String sourceCode;

        /**
         * 来源ID
         */
        @NotBlank(message = "来源ID不能为空")
        @Size(max = 19, message = "来源ID最大长度不能超过19位")
        private String sourceId;

        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 30, message = "来源类型最大长度不能超过30位")
        private String sourceType;

        /**
         * 入库类型
         */
        @NotBlank(message = "入库类型不能为空")
        @Size(max = 30, message = "入库类型最大长度不能超过30位")
        private String instockType;

        /**
         * 入库状态
         */
        @NotBlank(message = "入库状态不能为空")
        @Size(max = 64, message = "入库状态最大长度不能超过64位")
        private String instockStatus;

        /**
         * 发货仓名称
         */
        @NotBlank(message = "发货仓名称不能为空")
        @Size(max = 255, message = "发货仓名称最大长度不能超过255位")
        private String deliveryWarehouseName;

        /**
         * 发货仓ID
         */
        @NotBlank(message = "发货仓ID不能为空")
        @Size(max = 19, message = "发货仓ID最大长度不能超过19位")
        private String deliveryWarehouseId;

        /**
         * 中转仓名称
         */
        @NotBlank(message = "中转仓名称不能为空")
        @Size(max = 255, message = "中转仓名称最大长度不能超过255位")
        private String transferWarehouseName;

        /**
         * 中转仓ID
         */
        @NotBlank(message = "中转仓ID不能为空")
        @Size(max = 19, message = "中转仓ID最大长度不能超过19位")
        private String transferWarehouseId;

        /**
         * 目的仓名称
         */
        @NotBlank(message = "目的仓名称不能为空")
        @Size(max = 255, message = "目的仓名称最大长度不能超过255位")
        private String toWarehouseName;

        /**
         * 目的仓ID
         */
        @NotBlank(message = "目的仓ID不能为空")
        @Size(max = 19, message = "目的仓ID最大长度不能超过19位")
        private String toWarehouseId;

        /**
         * 物流方式
         */
        @NotBlank(message = "物流方式不能为空")
        @Size(max = 64, message = "物流方式最大长度不能超过64位")
        private String logisticsMethod;

        /**
         * 备注
         */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255, message = "备注最大长度不能超过255位")
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
        @Size(max = 255, message = "手动完结原因最大长度不能超过255位")
        private String finishReason;

        /**
         * 完结状态: not=未完结, auto=自动完结，manual=手动完结
         */
        @NotBlank(message = "完结状态: not=未完结, auto=自动完结，manual=手动完结不能为空")
        @Size(max = 64, message = "完结状态: not=未完结, auto=自动完结，manual=手动完结最大长度不能超过64位")
        private String finishStatus;

        /**
         * 第三方唯一编码
         */
        @NotBlank(message = "第三方唯一编码不能为空")
        @Size(max = 255, message = "第三方唯一编码最大长度不能超过255位")
        private String overseasWarehouseInboundId;


    }

    /**
     * 查询收货记录返回值
     */
    @Data
    @NoArgsConstructor
    public static class ReceiveRecordView {
        /**
         * 签收时间
         */
        private LocalDateTime receiveTime;
        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 签收数量
         */
        private String receiveUser;
    }


    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class ViewListReqDTO {

        /**
         * 目标ID类型:
         * mainId=单据ID
         * detailId=详情ID
         */
        @NotNull(message = "目标ID类型不能为空")
        @JsonDeserialize(using = RequestIdTypeEnum.RequestIdEnumDeserializer.class)
        private RequestIdTypeEnum requestIdType;

        /**
         * 请求ID列表
         */
        @NotNull(message = "请求ID列表不能为空")
        @Size(min = 1, message = "请求ID至少有一个")
        private List<@NotBlank(message = "请求ID不能为空") String> requestIdList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountDTO {
        /**
         * 入库单状态:
         * OutstockTypeEnum
         * 获取路径：/wms/common/enumDropDown?type=OverseasInstockStatus
         * toBeShipped=待发货，toBeSigned=待签收，partialSigned=部分签收，signed=已签收，canceled=已取消，abnormal=异常，
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;

    }
}