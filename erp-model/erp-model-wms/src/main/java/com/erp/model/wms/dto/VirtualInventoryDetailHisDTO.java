package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 虚拟仓库存历史信息请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class VirtualInventoryDetailHisDTO implements Serializable {




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
        * 快照日期
        */
        private LocalDate date;

        /**
         * 入库日期
         */
        private LocalDate billDate;

        /**
        * 数量
        */
        private Integer qty;

        /**
         * 批次剩余数量（平均库龄逆推）
         */
        private Integer waitQty;

        /**
         * 库龄(天)
         */
        private Integer inventoryAgeDays;

        /**
         * 虚拟库存明细id
         */
        private String virtualInventoryDetailId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 实体仓id
         */
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
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
         * 快照日期
         */
        @NotNull(message = "快照日期不能为空")
        private LocalDate date;
        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        private Integer qty;
        /**
         * 虚拟仓流水id
         */
        @NotBlank(message = "虚拟仓流水id不能为空")
        private String virtualInventoryDetailId;

        /**
         * 批次剩余数量（平均库龄逆推）
         */
        private Integer waitQty;

        /**
         * 库龄
         */
        private Integer inventoryAgeDays;


    }


    /**
     * 参数DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamDTO {
        /**
         * skuId集合
         */
        private List<String> skuIdList;
        /**
         * 仓库Id集合
         */
        private List<String> warehouseIdList;
        /**
         * 虚拟仓Id集合
         */
        private List<String> virtualWarehouseIdList;
        /**
         * 结束时间
         */
        private LocalDate endDate;
    }
}