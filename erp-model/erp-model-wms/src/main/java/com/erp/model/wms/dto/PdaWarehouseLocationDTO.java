package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class PdaWarehouseLocationDTO {
    private PdaWarehouseLocationDTO() {
        throw new IllegalStateException("Utility PdaWarehouseLocationDTO class");
    }
    @Data
    @NoArgsConstructor
    public static class WarehouseAreaDTO {
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 区位信息
         */
        private List<AreaDTO> areaList;

    }


    @Data
    @NoArgsConstructor
    public static class AreaDTO {
        /**
         * 区位id
         */
        private String areaId;
        /**
         * 区位名称
         */
        private String areaName;

    }

    @Data
    @NoArgsConstructor
    public static class WarehouseLocationAddDTO {
        /**
         * 区位Id集合
         */
        private List<String> areaIdList;
        /**
         * 仓位
         */
        @NotBlank(message = "仓位不能为空")
        private String warehouseLocation;
    }
}
