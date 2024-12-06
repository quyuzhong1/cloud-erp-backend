package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
        * skuId
        */
        private String skuId;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 虚拟仓id
        */
        private String virtualWarehouseId;

        /**
        * 快照日期
        */
        private LocalDate date;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 库存状态
        */
        private String dictInventoryStatus;

        /**
        * 虚拟仓流水id
        */
        private String virtualTransFlowId;

        /**
        * 批次号
        */
        private String batchNo;

        /**
        * 单据日期
        */
        private LocalDate billDate;


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

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 虚拟仓id
        */
        @NotBlank(message = "虚拟仓id不能为空")
        @Size(max = 19,message = "虚拟仓id最大长度不能超过19位")
        private String virtualWarehouseId;

        /**
        * 快照日期
        */
        private LocalDate date;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 库存状态
        */
        @NotBlank(message = "库存状态不能为空")
        @Size(max = 32,message = "库存状态最大长度不能超过32位")
        private String dictInventoryStatus;

        /**
        * 虚拟仓流水id
        */
        @NotBlank(message = "虚拟仓流水id不能为空")
        @Size(max = 19,message = "虚拟仓流水id最大长度不能超过19位")
        private String virtualTransFlowId;

        /**
        * 批次号
        */
        @NotBlank(message = "批次号不能为空")
        @Size(max = 32,message = "批次号最大长度不能超过32位")
        private String batchNo;

        /**
        * 单据日期
        */
        private LocalDate billDate;


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