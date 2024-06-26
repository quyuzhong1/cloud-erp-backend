package com.erp.model.wms.dto;
/**
 * 拣货波次
 * @date 2024-06-26
 * @author tanmujin
 */

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PickingWaveDTO implements Serializable {
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
        private String pickCartType;

        /**
         * 波次类型
         */
        private String waveType;
    }
}
