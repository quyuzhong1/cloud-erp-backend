package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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
         * falsehood 虚假发货
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

        private Integer qty;

        private String sourceSkuId;


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
         * 拣货单状态 中文
         */
        private String printPickingName;
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
         * 组包状态  not 不需要  wait 待组包   already 已经组包
         *
         */
        private String packageStatus;
        /**
         * 中转状态 not 不需要  wait 待中转   already 已经中转
         */
        private String transferStatus;


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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrintPickingViewDTO that = (PrintPickingViewDTO) o;
            return Objects.equals(skuId, that.skuId) && Objects.equals(skuNo, that.skuNo) && Objects.equals(productName, that.productName) && Objects.equals(warehouseId, that.warehouseId) && Objects.equals(warehouseName, that.warehouseName) && Objects.equals(warehouseLocation, that.warehouseLocation) && Objects.equals(remark, that.remark);
        }

        @Override
        public int hashCode() {
            return Objects.hash(skuId, skuNo, productName, warehouseId, warehouseName, warehouseLocation, remark);
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
}