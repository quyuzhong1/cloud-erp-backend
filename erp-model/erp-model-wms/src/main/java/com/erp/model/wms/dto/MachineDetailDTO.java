package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/10 14:04
 */
@Data
@NoArgsConstructor
public class MachineDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * skuId
         */
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;
        /**
         * 数量
         */
        private Integer qty;

        /**
         * 库位id
         */
        private String warehouseLocation;
        /**
         * 参照版本
         */
        private String referenceVersion;
        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO{

        /**
         * 子件信息
         */
        @Valid
        private List<MachineSubComponentsDTO.AddDTO> subComponentsDetailList;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 子件信息
         */
        @Valid
        private List<MachineSubComponentsDTO.UpdateDTO> subComponentsDetailList;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends UpdateDTO {

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 即时库存
         */
        private Integer curInventoryQty;

    }
}
