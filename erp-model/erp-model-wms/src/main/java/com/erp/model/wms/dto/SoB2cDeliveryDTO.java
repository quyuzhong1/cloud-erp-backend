package com.erp.model.wms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.*;

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

        /**
         * 详情
         */
        private List<SoB2cDeliveryDetailDTO.AddDTO> detailList;
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

        /**
         * 明细信息
         */
        @NotEmpty(message = "明细信息不能为空")
        @Valid
        private List<SoB2cDeliveryDetailDTO.AddDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
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
    public static class PagingParamDTO extends SortDTO {
        /**
         * tab
         */
        private String tabFlag;
        /**
         * 单号
         */
        private String code;
        /**
         * 平台集合（platform字典类型）http://172.16.100.11:3002/project/110/interface/api/13435
         */
        private List<String> PlatformList;
        /**
         * 店铺
         * 地址：http://172.16.100.11:3002/project/110/interface/api/24424
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
         * 拣货类型：/wms/common/enumDropDown?type=PickingType
         */
        private List<String> pickingTypeList;
        /**
         * 物流渠道
         * 地址：http://172.16.100.11:3002/project/128/interface/api/25621
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
         * 地址：http://172.16.100.11:3002/project/92/interface/api/26953
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
         * 发货单号【可排序】
         */
        private String code;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 拦截标识：true表示拦截
         */
        private Boolean isIntercept;
        /**
         * 平台【可排序】
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
         * 店铺名称【可排序】
         */
        private String shopName;
        /**
         * 销售单号【可排序】
         */
        private String soCode;
        /**
         * 状态【可排序】
         */
        private String status;
        /**
         * 状态中文
         */
        private String statusName;
        /**
         * 拣货类型【可排序】
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
         * 物流渠道名称【可排序】
         */
        private String logisticsChannelName;
        /**
         * 是否打印拣货单【可排序】
         */
        private Boolean isPrintPicking;
        /**
         * 是否验货【可排序】
         */
        private Boolean isInspection;
        /**
         * 是否称重【可排序】
         */
        private Boolean isWeigh;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 产品编号【可排序】
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 发货数量【可排序】
         */
        private Integer deliveryQty;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称【可排序】
         */
        private String warehouseName;
        /**
         * 仓位【可排序】
         */
        private String warehouseLocation;
        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;
        /**
         * 发货时间【可排序】
         */
        private LocalDateTime deliveryTime;

    }
    /**
     * 打印拣货单
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
         * 打印类型 wms/common/enumDropDown?type=SoB2cDeliveryPrintType
         * logisticsWaybill: 物流面单 ，distribution：配货单，all：全部
         */
        private String printType;
        /**
         * 渠道id
         */
        private String logisticsChannelId;
        /**
         * 渠道名称
         */
        private String logisticsChannelName;
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
     * 打印物流面单详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintLogisticsWaybillDetailDTO {
        /**
         * 打印类型 wms/common/enumDropDown?type=SoB2cDeliveryPrintType
         * logisticsWaybill: 物流面单 ，distribution：配货单，all：全部
         */
        private String printType;
        /**
         * 销售单id
         */
        private String soB2cId;
        /**
         * 物流商名称
         */
        private String logisticsSupplierName;
        /**
         * 渠道id
         */
        private String logisticsChannelId;
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

    /**
     * 打印配货单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintDistributionDTO {
        /**
         * 打印类型 wms/common/enumDropDown?type=SoB2cDeliveryPrintType
         * logisticsWaybill: 物流面单 ，distribution：配货单，all：全部
         */
        private String printType;
        /**
         * 渠道id
         */
        private String logisticsChannelId;
        /**
         * 渠道名称
         */
        private String logisticsChannelName;
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
        private List<PrintDistributionDetailDTO> detailList;
    }

    /**
     * 打印配货单详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintDistributionDetailDTO {
        /**
         * 销售单id
         */
        private String soB2cId;
        /**
         * 物流商名称
         */
        private String logisticsSupplierName;
        /**
         * 渠道id
         */
        private String logisticsChannelId;
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

    /**
     * 打印物流面单确认
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintLogisticsBillConfirmDTO {
        /**
         * 打印类型 wms/common/enumDropDown?type=SoB2cDeliveryPrintType
         * logisticsWaybill: 物流面单 ，distribution：配货单，all：全部
         */
        private String printType;
        /**
         * 详情
         */
        private List<LogisticsChannelDTO> detailList;
    }


    /**
     * 打印物流面单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogisticsChannelDTO {
        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 详情
         */
        private List<PrintLogisticsWaybillDetailDTO> detailList;
    }
}