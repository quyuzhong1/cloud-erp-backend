package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * fba货件装箱信息请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
*/
@Data
@NoArgsConstructor
public class FbaShipmentPackingDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 货件号
         */
        private String code;

        /**
         * 货件箱号
         */
        private String boxNo;

        /**
         * 装箱平台sku
         */
        private String packingPlatformSku;

        /**
         * 装箱FNSKU
         */
        private String packingFnSku;
    }

    @Data
    @NoArgsConstructor
    public static class PackingDTO {

        /**
         * 文件路径
         */
        private String fileUrl;

        /**
         * 货件号
         */
        private String fbaShipmentCode;

        /**
         * 货件箱号
         */
        private String boxNo;

        /**
         * 装箱明细
         */
        private List<PackingDetailDTO> detailDTOList;
    }

    @Data
    @NoArgsConstructor
    public static class PackingDetailDTO {
        /**
         * 平台产品id（ASIN）
         */
        private String asin;
        /**
         * 平台sku（msku）
         */
        private String msku;
        /**
         * FNSKU
         */
        private String fnSku;
        /**
         * 装箱数量
         */
        private Integer qty;

    }
}