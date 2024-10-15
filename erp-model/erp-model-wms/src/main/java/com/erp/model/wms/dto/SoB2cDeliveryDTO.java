package com.erp.model.wms.dto;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.AbnormalCauseEnum;
import com.erp.model.wms.enums.B2cDeliveryLogisticTypeEnum;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Data
    @NoArgsConstructor
    public static class DeliverDTO{
        /**
         * manual 手动
         * falsehood 手动标发
         */
      private String type;

      private List<String> ids;
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
         * 长
         */
        private BigDecimal length;
        /**
         * 宽
         */
        private BigDecimal width;
        /**
         * 高
         */
        private BigDecimal height;

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
    @AllArgsConstructor
    @Builder
    public static class UpdateWeightDTO {

        private String soId;

        private BigDecimal weight;

        private String weightUnit;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 物流类型
         */
        @StateEnumValue(clazz = B2cDeliveryLogisticTypeEnum.class, message = "物流类型有误")
        private String logisticType;

        /**
         * 详情
         */
        private List<SoB2cDeliveryDetailDTO.AddDTO> detailList;
    }


    /**
     * 发货的sku 信息
     */
    @Data
    @NoArgsConstructor
    public static class DeliverySkuDTO  {


        private String skuId;

        private String skuNo;

        private String platformSkuNo;

        private String detailId;

        private Integer qty;

        private String sourceSkuId;

        private String warehouseId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeliverySkuDTO that = (DeliverySkuDTO) o;
            return Objects.equals(skuId, that.skuId) && Objects.equals(skuNo, that.skuNo) && Objects.equals(platformSkuNo, that.platformSkuNo) && Objects.equals(qty, that.qty) && Objects.equals(sourceSkuId, that.sourceSkuId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(skuId, skuNo, platformSkuNo, qty, sourceSkuId);
        }
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
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名
         */
        private String shopName;

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

        /**
        * 备注
        */
        private String remark;
        /**
         * 物流渠道id
         */
        @NotBlank(message = "物流渠道id不能为空")
        private String  logisticsChannelId;
        /**
         * 物流渠道名
         */
        private String  logisticsChannelName;

        /**
         * 运输单号
         */
        private String  transportNo;

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
         * 描述：waitHandle:待处理, picking:拣货中, falseShipment:手动标发, shipped:已发货, cancelDelivery:取消发货,intercepting ：拦截中
         */
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
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

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
         * 发货拦截单id
         */
        private String interceptId;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 发货单号【可排序】
         */
        private String code;
        /**
         * 波次号
         */
        private String waveCode;
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
         * 是否打印物流单【可排序】
         */
        private Boolean isPrintLogistic;
        /**
         * 拣货单状态 中文
         */
        private String printPickingName;
        /**
         * 打印物流单状态 中文
         */
        private String printLogisticName;
        /**
         * 是否验货【可排序】
         */
        private Boolean isInspection;
        /**
         * 验货状态 中文
         */
        private String inspectionName;
        /**
         * 是否称重【可排序】
         */
        private Boolean isWeigh;
        /**
         * 称重状态 中文
         */
        private String weighName;
        /**
         * 称重重量
         */
        private BigDecimal weight;
        /**
         * 重量+单位
         */
        private String weightName;
        /**
         * 重量单位
         */
        private String weightUnit;
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

        /**
         * 完成打印时间【可排序】
         */
        private LocalDateTime finishPrintTime;

        /**
         * 验货时间【可排序】
         */
        private LocalDateTime inspectionTime;

        /**
         * 称重时间【可排序】
         */
        private LocalDateTime weighingTime;

        /**
         * 平台订单号【可排序】
         */
        private String platformCode;

        /**
         * 运单号
         */
        private String logisticsCode;

        /**
         * 跟踪号
         */
        private String trackCode;

        /**
         * 订单备注
         */
        private String orderRemark;

        /**
         * 组包状态  not 不需要  wait 待组包   already 已经组包
         *
         */
        private String packageStatus;
        /**
         * 中转状态 not 不需要  wait 待中转   already 已经中转
         */
        private String transferStatus;
        /**
         * 异常原因
         */
        @Dict(enumClass = AbnormalCauseEnum.class)
        private String abnormalCause;

    }

    /**
     * 打印拣货单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintPickingMainViewDTO {
        /**
         * 波次号
         */
        private String waveCode;
        /**
         * 订单数量
         */
        private Integer orderCount;
        /**
         * 拣货单信息
         */
        private List<PrintPickingViewDTO> detailList;
    }


    /**
     * 打印配货单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocateCargoViewDTO {
        /**
         *发货单号
         */
        private String deliveryCode;
        /**
         *销售订单id
         */
        private String soId;
        /**
         * 产品id
         */
        private String skuId;
        /**
         * 产品编码
         */
        private String skuNo;
        /**
         * 拣货数量
         */
        private Integer pickingQty;
        /**
         * 推荐仓位
         */
        private String warehouseLocation;
        /**
         * 是否缺货
         */
        private Boolean isOutStock;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AllocateCargoViewDTO that = (AllocateCargoViewDTO) o;
            return Objects.equals(deliveryCode, that.deliveryCode) && Objects.equals(soId, that.soId) && Objects.equals(skuId, that.skuId) && Objects.equals(skuNo, that.skuNo) && Objects.equals(warehouseLocation, that.warehouseLocation) && Objects.equals(isOutStock, that.isOutStock);
        }

        @Override
        public int hashCode() {
            return Objects.hash(deliveryCode, soId, skuId, skuNo, warehouseLocation, isOutStock);
        }
    }


    /**
     * 打印拣货单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintPickingViewDTO {
        /**
         * 发货单id
         */
        private String deliveryId;
        /**
         * 发货单id集合
         */
        private List<String> deliveryIdList;
        /**
         * 波次号
         */
        private String waveCode;
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
        /**
         * 是否缺货
         */
        private Boolean isOutStock;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrintPickingViewDTO that = (PrintPickingViewDTO) o;
            return Objects.equals(waveCode, that.waveCode) && Objects.equals(skuId, that.skuId) && Objects.equals(skuNo, that.skuNo) && Objects.equals(productName, that.productName) && Objects.equals(warehouseId, that.warehouseId) && Objects.equals(warehouseName, that.warehouseName) && Objects.equals(warehouseLocation, that.warehouseLocation) ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(waveCode,skuId, skuNo, productName, warehouseId, warehouseName, warehouseLocation);
        }
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
         * logisticsBill: 物流面单 ，allocateCargoBill：配货单，all：全部
         */
        private String printType;
        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 是否支持打印面单
         */
        private Boolean printLabel;

        /**
         * 是否支持打印配货单
         */
        private Boolean printDelivery;
        /**
         * 配货单打印类型
         * custom 自定义
         * authority 官方标签
         */
        private String printDeliveryType;
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
         * 是否禁用 true 禁用
         */
        private Boolean disabled;
        /**
         * 错误原因
         */
        private String errorMsg;

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
         * 发货单id
         */
        private String id;
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
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 物流类型
         */
        private String logisticType;
        /**
         * 打印排序
         */
        private Integer index;
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
         * logisticsBill: 物流面单 ，allocateCargoBill：配货单，all：全部
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
         * logisticsBill: 物流面单，allocateCargoBill：配货单
         */
        private String printType;
        /**
         * 详情
         */
        private List<PrintLogisticsWaybillDetailDTO> detailList;
    }

    /**
     * 打印物流面单确认
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintLogisticsBillConfirmParam {
        /**
         * 打印类型 wms/common/enumDropDown?type=SoB2cDeliveryPrintType
         * logisticsBill: 物流面单，allocateCargoBill：配货单
         */
        private String printType;

        private List<String> ids;
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


    }

    @Getter
    @Setter
    public static class GenerationWavesDTO {

        /**
         * 拣货方式
         */
        @NotBlank(message = "拣货方式不能为空")
        private String pickingType;
        /**
         * 拣货车类型
         */
        @NotBlank(message = "拣货车类型不能为空")
        private String pickingCartTypeId;
        /**
         * 发货单号
         */
        @Size(min = 1, message = "发货单不能为空")
        private List<String> ids;

        @NotNull(message = "波次订单数量不能为空")
        @Min(value = 1, message = "波次订单数量最小为1")
        @Max(value = 9999, message = "波次订单数量最大为9999")
        private Integer num;

        private Boolean atuoAemainder = false;
    }

    @Getter
    @Setter
    public static class CancelShipmentView {

        /**
         * 发货单
         */
        @Size(min = 1, message = "发货单不能为空")
        @Valid
        @Dict
        private List<CancelShipmentDTO> cancelShipments;
    }

    @Getter
    @Setter
    public static class CancelShipmentDTO {
        /**
         * 发货单id
         */
        private String id;
        /**
         * 发货单明细id
         */
        private String detailId;
        /**
         * 发货单号
         */
        private String code;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 仓库id
         */
        @Dict(queryFieldName = "id", tableName = "warehouse")
        private String warehouseId;
        /**
         * 拣货仓位
         */
        private String warehouseLocation;
        /**
         * 拣货仓位名称
         */
        private String warehouseLocationName;
        /**
         * 拣货数量
         */
        private Integer pickingQty;
        /**
         * 返还仓位
         */
        private String returnWarehouseLocation;
        /**
         * 返还仓位
         */
        private String returnWarehouseLocationId;
        /**
         * 返还仓位名称
         */
        private String returnWarehouseLocationName;
        /**
         * 返还数量
         */
        private Integer returnQty;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandleErrorDTO {
        /**
         * ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 是否添加库存
         */
        @NotNull(message = "是否添加库存")
        private Boolean isAddQty;
    }


}