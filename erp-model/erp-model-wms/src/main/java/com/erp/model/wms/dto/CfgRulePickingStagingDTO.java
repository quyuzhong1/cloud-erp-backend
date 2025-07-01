package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 虚拟仓设置
 */
@Data
@NoArgsConstructor
public class CfgRulePickingStagingDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 单据类型
         * PickingBillTypeEnum
         */
        private String billType;
        /**
         * 库区id
         */
        private String warehouseAreaId;
        /**
         * 库位id
         */
        private String warehouseLocationId;
        /**
         * 库位
         */
        private String warehouseLocation;
    }

    @Data
    @NoArgsConstructor
    public static class StagingDTO{
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * B2B库位id
         */
        @NotBlank(message = "B2B库位id不能为空")
        private String b2bWarehouseLocationId;
        /**
         * B2B库位
         */
        private String b2bWarehouseLocationName;
        /**
         * FBA库位id
         */
        @NotBlank(message = "FBA库位id不能为空")
        private String fbaWarehouseLocationId;
        /**
         * FBA库位
         */
        private String fbaWarehouseLocationName;
        /**
         * 三方仓库位id
         */
        @NotBlank(message = "三方仓库位id不能为空")
        private String thirdWarehouseLocationId;
        /**
         * 三方仓库位
         */
        private String thirdWarehouseLocationName;
    }
}