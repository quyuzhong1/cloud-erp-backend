package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 波次拣货DTO
 * @date 2024-06-20
 * @author tanmujin
 */
@Data
@NoArgsConstructor
public class WavePickingDTO implements Serializable {

    @Data
    public static class searchParamDTO {
        /**
         * 波次编码
         */
        private String waveCode;

        /**
         * 波次名称
         */
        private String waveName;

        /**
         * 波次类型
         */
        private String waveTypeCode;

        /**
         * 拣货车类型
         */
        private String pickingTruckType;

        /**
         * 拣货车编码
         */
        private String pickingTruckCode;

        /**
         * 分拣方式
         */
        private String pickingType;

        /**
         * 波次状态
         */
        private String waveStatusCode;

        /**
         * 打印状态
         */
        private String printStatus;

        /**
         * 是否缺货
         */
        private String isSoldOut;

        /**
         * 创建人
         */
        private String createUser;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 拣货人
         */
        private String pickingUser;

        /**
         * 拣货时间
         */
        private String pickingTime;
    }

    @Data
    public static class ViewDTO {
        /**
         * 波次编码
         */
        private String waveCode;

        /**
         * 波次名称
         */
        private String waveName;

        /**
         * 波次类型
         */
        private String waveTypeName;

        /**
         * 拣货车类型
         */
        private String pickingTruckType;

        /**
         * 拣货车编码
         */
        private String pickingTruckCode;

        /**
         * 分拣方式
         */
        private String pickingType;

        /**
         * 波次状态
         */
        private String waveStatusName;

        /**
         * 打印状态
         */
        private String printStatus;

        /**
         * 是否缺货
         */
        private String isSoldOut;

        /**
         * 操作人
         */
        private String operateUser;

        /**
         * 操作时间
         */
        private String operateTime;
    }

    @Data
    public static class DetailViewDTO {
        /**
         * 波次编码
         */
        private String waveCode;

        /**
         * 销售订单编号
         */
        private String soCode;

        /**
         * 发货单号
         */
        private String soB2cDeliveryCode;

        /**
         * 拣货状态
         */
        private String pickingStatus;

        /**
         * 物流渠道
         */
        private String logisticsChannelName;

        /**
         * SKU
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
         * 拣货仓位信息
         */
        private List<PickingLocationInfoDTO> pickingLocationInfoList;
    }

    @Data
    public static class PickingLocationInfoDTO {
        /**
         * 仓位名称
         */
        private String locationName;

        /**
         * 仓位编码
         */
        private String locationCode;

        /**
         * 是否缺货
         */
        private String isSoldOut;

        /**
         * 应拣数量
         */
        private Integer shouldPickQty;

        /**
         * 已拣数量
         */
        private Integer pickedQty;
    }

    @Data
    public static class moveOutDTO {
        /**
         * 波次编码
         */
        private String waveCode;

        /**
         * 发货单号
         */
        private String soB2cDeliveryCode;
    }

    @Data
    public static class addDTO {
        /**
         * 波次编码
         */
        private String waveCode;

        /**
         * 发货单号集合
         */
        private List<String> deliveryCodeList;

        /**
         * 分拣方式
         */
        private String pickingType;

        /**
         * 拣货车类型
         */
        private String pickingTruckType;
    }
}
