package com.erp.model.wms.dto;

import com.common.business.annotation.Dict;
import com.erp.model.wms.enums.WavePickingTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 波次列表详情
 * @date 2024-06-30
 * @author tanmujin
 */
@Data
@NoArgsConstructor
public class WaveListDetailDTO implements Serializable {

    @Data
    public static class ViewDTO{
        /**
         * 波次ID
         */
        private String id;
        /**
         * 波次编码
         */
        private String code;

        /**
         * 波次名称
         */
        private String name;

        /**
         * 仓位ID
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 拣货车类型
         */
        private String pickingCartType;

        /**
         * 拣货车类型名称
         */
        private String pickingCartTypeName;

        /**
         * 拣货车编号
         */
        private String pickingCartCode;

        /**
         * 拣货方式
         */
        @Dict(enumClass = WavePickingTypeEnum.class)
        private String pickingType;

        /**
         * 波次状态
         */
//        @Dict(enumClass = WaveStatusEnum.class)
        private String status;

        /**
         * 波次状态名称
         */
        private String statusName;

        /**
         * 已检数量
         */
        private Integer totalPickedQty;

        /**
         * 发货单列表
         */
        private List<WaveListDetailDTO.DeliveryInfoDTO> deliveryInfoList;
    }

    @Data
    public static class MoveOutDTO {

        /**
         * 波次ID
         */
        private String waveId;

        /**
         * 发货单号Id
         */
        private String deliveryId;
    }

    @Data
    public static class DeliveryInfoDTO {
        private String id;

        /**
         * 框号
         */
        private String basketNo;

        /**
         * 销售订单ID
         */
        private String soId;
        /**
         * 销售订单编号
         */
        private String soCode;

        /**
         * 发货单ID
         */
        private String deliveryId;
        /**
         * 发货单号
         */
        private String deliveryCode;

        /**
         * 拣货状态
         */
        private String pickingStatus;

        /**
         * 拣货状态名称
         */
        private String pickingStatusName;

        /**
         * 物流渠道
         */
        private String logisticsChannelName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku 编码
         */
        private String skuNo;

        /**
         * 销售数量
         */
        private Integer salesQty;

        /**
         * 已拣数量汇总
         */
        private Integer pickedSumQty;

        /**
         * 仓位信息
         */
        private List<LocationInfoDTO> locationInfoList;
    }

    @Data
    public static class SkuInfoDTO{
        private String skuId;

        private String skuNo;

        /**
         * 销售数量
         */
        private Integer salesQty;

        /**
         * 已拣数量汇总
         */
        private Integer pickedSumQty;
    }

    @Data
    public static class LocationInfoDTO{
        /**
         * 拣货库区
         */
        private String warehouseArea;

        /**
         * 拣货库区名称
         */
        private String warehouseAreaName;

        /**
         * 拣货仓位
         */
        private String warehouseLocation;

        /**
         * 拣货仓位名称
         */
        private String warehouseLocationName;

        /**
         * 应拣
         */
        private Integer shouldPickQty;

        /**
         * 已拣
         */
        private Integer pickedQty;

        /**
         * 是否缺货
         */
        private Boolean isOutStock;
    }
}
