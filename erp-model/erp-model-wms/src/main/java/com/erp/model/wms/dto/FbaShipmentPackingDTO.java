package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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