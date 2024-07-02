package com.erp.model.wms.dto;

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
         * 拣货车编号
         */
        private String pickingCartCode;

        /**
         * 拣货方式
         */
        private String pickingType;

        /**
         * 波次状态
         */
        private String status;

        /**
         * 发货单列表
         */
        private List<WaveListDTO.DeliveryInfoDTO> deliveryInfoList;
    }

    @Data
    public static class MoveOutDTO {

        /**
         * 波次ID
         */
        private String waveId;

        /**
         * 发货单号
         */
        private String soB2cDeliveryCode;
    }
}
