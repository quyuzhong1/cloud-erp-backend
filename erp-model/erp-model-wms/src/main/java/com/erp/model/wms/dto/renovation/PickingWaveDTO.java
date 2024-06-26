package com.erp.model.wms.dto.renovation;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PickingWaveDTO {
    @Data
    public static class AddDTO {
        /**
         * 波次编码
         */
        private String code;

        /**
         * 发货单号集合
         */
        private List<String> deliveryIdList;

        /**
         * 分拣方式
         */
        private String pickingType;

        /**
         * 拣货车类型
         */
        private String pickCartTypeId;

        /**
         * 波次类型
         */
        private String waveType;
    }


    @Getter
    @Setter
    public static class PickingWaveDetailDTO {

        /**
         * 波次id
         */
        private String mainId;
        /**
         * 篮号
         */
        private String basketNo;

        /**
         * 发货id
         */
        private String deliveryId;
        /**
         * 拣货明细id
         */
        private String pickDetailId;
        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;
        /**
         * 已拣货数量
         */
        private Integer pickedQty;
        /**
         * 已分货数量
         */
        private Integer allocatedQty;
    }
}
