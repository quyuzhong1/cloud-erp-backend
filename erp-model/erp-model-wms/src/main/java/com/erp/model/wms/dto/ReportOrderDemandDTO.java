package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-09-23
*/
@Data
@NoArgsConstructor
public class ReportOrderDemandDTO implements Serializable {


    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 联合id
         */
        private String unionId;

        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;

        /**
         * 实体仓名称
         */
        private String warehouseName;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 剩余需求总数
         */
        private Integer totalQty;

        /**
         * B2B需求数
         */
        private Integer soQty;

        /**
         * B2C需求数
         */
        private Integer b2cSoQty;

        /**
         * 头程需求数
         */
        private Integer firstMileQty;

        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;

        /**
         * 是否缺货，true是，false否
         */
        private Boolean isVirtualScarce;

        /**
         * 缺货数量
         */
        private Integer virtualScarceQty;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

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
        * 仓库id 
        */
        private String warehouseId;

        /**
        * 虚拟仓库id
        */
        private String virtualWarehouseId;

        /**
        * 虚拟出库名称
        */
        private String virtualWarehouseName;

        /**
        * 实体仓名称
        */
        private String warehouseName;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 剩余需求总数
        */
        private Integer totalQty;

        /**
        * B2B销售订单需求数
        */
        private Integer soQty;

        /**
        * B2C销售订单需求数
        */
        private Integer b2cSoQty;

        /**
        * 头程需求数
        */
        private Integer firstMileQty;

        /**
        * 虚拟仓可用库存
        */
        private Integer virtualUsableQty;

        /**
        * 是否缺货，true是，false否
        */
        private Boolean isVirtualScarce;

        /**
        * 缺货数量
        */
        private Integer virtualScarceQty;


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
        * 仓库id 
        */
        @NotBlank(message = "仓库id 不能为空")
        @Size(max = 19,message = "仓库id 最大长度不能超过19位")
        private String warehouseId;

        /**
        * 虚拟仓库id
        */
        @NotBlank(message = "虚拟仓库id不能为空")
        @Size(max = 19,message = "虚拟仓库id最大长度不能超过19位")
        private String virtualWarehouseId;

        /**
        * 虚拟出库名称
        */
        @NotBlank(message = "虚拟出库名称不能为空")
        @Size(max = 32,message = "虚拟出库名称最大长度不能超过32位")
        private String virtualWarehouseName;

        /**
        * 实体仓名称
        */
        @NotBlank(message = "实体仓名称不能为空")
        @Size(max = 32,message = "实体仓名称最大长度不能超过32位")
        private String warehouseName;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 64,message = "产品名称最大长度不能超过64位")
        private String productName;

        /**
        * 剩余需求总数
        */
        @NotNull(message = "剩余需求总数不能为空")
        private Integer totalQty;

        /**
        * B2B销售订单需求数
        */
        @NotNull(message = "B2B销售订单需求数不能为空")
        private Integer soQty;

        /**
        * B2C销售订单需求数
        */
        @NotNull(message = "B2C销售订单需求数不能为空")
        private Integer b2cSoQty;

        /**
        * 头程需求数
        */
        @NotNull(message = "头程需求数不能为空")
        private Integer firstMileQty;

        /**
        * 虚拟仓可用库存
        */
        @NotNull(message = "虚拟仓可用库存不能为空")
        private Integer virtualUsableQty;

        /**
        * 是否缺货，true是，false否
        */
        @NotNull(message = "是否缺货，true是，false否不能为空")
        private Boolean isVirtualScarce;

        /**
        * 缺货数量
        */
        @NotNull(message = "缺货数量不能为空")
        private Integer virtualScarceQty;


    }


}