package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 虚拟仓库存历史信息请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-12-10
*/
@Data
@NoArgsConstructor
public class VirtualInventoryHisDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 虚拟仓库存id
        */
        private String virtualInventoryId;

        /**
        * 快照日期
        */
        private LocalDate date;

        /**
        * 数量
        */
        private Integer qty;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 虚拟仓库存id
        */
        @NotBlank(message = "虚拟仓库存id不能为空")
        @Size(max = 19,message = "虚拟仓库存id最大长度不能超过19位")
        private String virtualInventoryId;

        /**
        * 快照日期
        */
        private LocalDate date;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;


    }

    @Data
    @NoArgsConstructor
    public static class VirtualQtyDTO {
        /**
         * SKUId
         */
        private String skuId;
        /**
         * 实体仓库
         */
        private String warehouseId;
        /**
         * 虚拟仓库
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓库存
         */
        private Integer virtualQty;
        /**
         * 快照日期
         */
        private LocalDate date;
    }
}