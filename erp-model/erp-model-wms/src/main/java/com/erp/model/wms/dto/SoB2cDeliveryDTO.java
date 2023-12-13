package com.erp.model.wms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * b2c发货单请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
*/
@Data
@NoArgsConstructor
public class SoB2cDeliveryDTO implements Serializable {




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
         * 发货单编号
         */
        private String code;
        /**
         * 单据状态
         */
        private String status;
        /**
         * 单据状态中文
         */
        private String statusName;
        /**
         * 来源单据id
         */
        private String sourceId;
        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 平台名称
         */
        private String dictPlatformName;
        /**
         * 店铺Id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 拣货类型
         */
        private String pickingType;
        /**
         * 拣货类型名称
         */
        private String pickingTypeName;
        /**
         * 物流渠道id
         */
        private String logisticsChannelId;
        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;
        /**
         * 运单号
         */
        private String transportNo;
        /**
         * 称重重量
         */
        private BigDecimal weight;
        /**
         * 称重重量单位
         */
        private String weightUnit;
        /**
         * 详情
         */
        private List<SoB2cDeliveryDetailDTO.ViewDTO> detailList;

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
        * 状态 waitHandle:待处理  picking:拣货中 falseShipment:虚假发货 shipped:已发货  cancelDelivery:取消发货
        */
        @NotBlank(message = "状态 waitHandle:待处理  picking:拣货中 falseShipment:虚假发货 shipped:已发货  cancelDelivery:取消发货不能为空")
        @Size(max = 25,message = "状态 waitHandle:待处理  picking:拣货中 falseShipment:虚假发货 shipped:已发货  cancelDelivery:取消发货最大长度不能超过25位")
        private String status;

        /**
        * 销售单号
        */
        @NotBlank(message = "销售单号不能为空")
        @Size(max = 50,message = "销售单号最大长度不能超过50位")
        private String soCode;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源单号
        */
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 50,message = "来源单号最大长度不能超过50位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 64,message = "来源类型最大长度不能超过64位")
        private String sourceType;

        /**
        * 平台
        */
        @NotBlank(message = "平台不能为空")
        @Size(max = 50,message = "平台最大长度不能超过50位")
        private String dictPlatform;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255,message = "店铺名称最大长度不能超过255位")
        private String shopName;

        /**
        * 拣货类型
        */
        @NotBlank(message = "拣货类型不能为空")
        @Size(max = 50,message = "拣货类型最大长度不能超过50位")
        private String pickingType;

        /**
        * 是否打印拣货单
        */
        @NotNull(message = "是否打印拣货单不能为空")
        private Boolean isPrintPicking;

        /**
        * 是否验货
        */
        @NotNull(message = "是否验货不能为空")
        private Boolean isInspection;

        /**
        * 是否称重
        */
        @NotNull(message = "是否称重不能为空")
        private Boolean isWeigh;

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
        @Size(max = 255,message = "物流渠道名称最大长度不能超过255位")
        private String logisticsChannelName;

        /**
        * 运单号
        */
        @NotBlank(message = "运单号不能为空")
        @Size(max = 64,message = "运单号最大长度不能超过64位")
        private String transportNo;

        /**
        * 称重重量
        */
        @NotNull(message = "称重重量不能为空")
        @Digits(integer = 6, fraction = 4, message = "称重重量整数位不能超过6位，小数位不能超过4位")
        private BigDecimal weight;

        /**
        * 单位
        */
        @NotBlank(message = "单位不能为空")
        @Size(max = 10,message = "单位最大长度不能超过10位")
        private String weightUnit;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;


    }

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 标识：wms/common/enumDropDown?type=SoB2cDeliveryStatus
         * 描述：waitHandle:待处理, picking:拣货中, falseShipment:虚假发货, shipped:已发货, cancelDelivery:取消发货
         */
        @StateEnumValue(clazz = SoB2cDeliveryStatusEnum.class, message = "tab类型有误")
        @NotBlank(message = "tab不能为空")
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 列表分页查询
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingParamDTO {
        /**
         * tab
         */
        private String tabFlag;
        /**
         * 单号
         */
        private String code;
        /**
         * 平台
         */
        private List<String> dictPlatformList;
        /**
         * 店铺
         */
        private List<String> shopIdList;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 状态
         */
        private List<String> statusList;
        /**
         * 拣货类型
         */
        private List<String> pickingTypeList;
        /**
         * 物流渠道
         */
        private List<String> logisticsChannelIdList;
        /**
         * 货件是否打印
         */
        private Boolean isPrintPicking;
        /**
         * 是否验货
         */
        private Boolean isInspection;
        /**
         * 是否称重
         */
        private Boolean isWeigh;
        /**
         * sku编号
         */
        private List<String> skuNoList;
        /**
         * 发货仓库id
         */
        private List<String> warehouseIdList;
        /**
         * 创建时间
         */
        private List<LocalDateTime> createTimeList;
        /**
         * 发货时间
         */
        private List<LocalDateTime> deliveryTimeList;
    }

    /**
     * 列表分页查询
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDTO {
        /**
         * 主表id
         */
        private String id;
        /**
         * 发货id
         */
        private String code;
        /**
         * 拦截标识：true表示拦截
         */
        private Boolean isIntercept;
        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 平台名称
         */
        private String dictPlatformName;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 状态
         */
        private String status;
        /**
         * 状态中文
         */
        private String statusName;
        /**
         * 拣货类型
         */
        private String pickingType;
        /**
         * 拣货类型中文
         */
        private String pickingTypeName;
        /**
         * 物流渠道id
         */
        private String logisticsChannelId;
        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;
        /**
         * 是否打印拣货单
         */
        private Boolean isPrintPicking;
        /**
         * 是否验货
         */
        private Boolean isInspection;
        /**
         * 是否称重
         */
        private Boolean isWeigh;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 产品编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;

    }
    /**
     * 列表分页查询
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintPickingViewDTO {
        /**
         * 产品id
         */
        private String skuId;
        /**
         * 产品编码
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 拣货数量
         */
        private Integer pickingQty;
        /**
         * 仓库Id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 推荐仓位
         */
        private String warehouseLocation;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 打印物流面单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintLogisticsWaybillDTO {
        /**
         * 打印类型
         * logisticsWaybill: 物流面单 ，distribution：配货单
         */
        private String printType;
        /**
         * 渠道名称
         */
        private List<String> logisticsChannelNameList;
        /**
         * 无运单号数量
         */
        private Integer notTransportNoNum;
        /**
         * 有运单号数量
         */
        private Integer isTransportNoNum;
        /**
         * 详情
         */
        private List<PrintLogisticsWaybillDetailDTO> detailList;
    }

    /**
     * 打印物流面单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintLogisticsWaybillDetailDTO {
        /**
         * 物流商名称
         */
        private String providerName;
        /**
         * 渠道名称
         */
        private String logisticsChannelName;
        /**
         * 订单编号
         */
        private String soCode;
        /**
         * 物流单号
         */
        private String transportNo;
    }
}