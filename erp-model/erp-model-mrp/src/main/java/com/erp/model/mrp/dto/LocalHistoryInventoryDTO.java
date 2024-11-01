package com.erp.model.mrp.dto;

import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class LocalHistoryInventoryDTO {

    /**
     * 即时库存分页列表
     */
    @Getter
    @Setter
    public static class PagingViewDTO {

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 产品id
         */
        private String productId;

        /**
         * 规格类型  1：无规格  2：多规格
         */
        private Integer specType;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 产品图片链接
         */
        private String productImgUrl;


        /**
         * spu编号
         */
        private String spuNo;

        /**
         * 库存组织id
         */
        private String orgId;

        /**
         * 库存组织名称
         */
        private String orgName;

        /**
         * 销售状态编码
         */
        private Integer saleState;

        /**
         * 销售状态名称
         */
        private String saleStateName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 实际库存数量
         * 实际库存=可用库存+冻结库存
         */
        private Integer realQty;

        /**
         * 可用库存数量
         */
        private Integer usableQty;

        /**
         * 冻结库存数量
         */
        private Integer frozenQty;

        /**
         * 在途库存数量
         */
        private Integer intransitQty;

        /**
         * 待检库存数量
         */
        private Integer waitqcQty;

        /**
         * 金蝶库存
         */
        private String kingdeeQty;

        /**
         * 库存差异
         */
        private String diffQty;

        /**
         * 仓位编码
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 库区编码
         */
        private String warehouseArea;

        /**
         * 库区名称
         */
        private String warehouseAreaName;

    }

    /**
     * 即时库存查询条件
     */
    @Getter
    @Setter
    public static class SearchParamDTO extends SortDTO {

        /**
         * 产品名称
         */
        private String productName;


        /**
         * sku编码
         */
        private List<String> skuNoList;


        /**
         * spu编码 接口地址：/wms/drop/down/product/spuNo/list（一次性返回所有）
         */
        private List<String> spuNoList;

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态集合 接口地址：plm/common/enumDropDown?type=SaleState
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织集合
         */
        private List<String> orgIdList;

        /**
         * 是否过滤0实际库存，默认前端页面勾上不显示0库存
         */
        private Boolean hideZeroInventory;

        /**
         * 查询维度：warehouse仓库，warehouseArea库区，warehouseLocation仓位<br/>
         * 接口地址：/wms/dict/drop/down?type=inventoryDimension
         */
        @NotBlank
        private String dimension;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 库区名称
         */
        private String warehouseAreaName;

        /**
         * SKU ID编码集合
         */
        private List<String> skuIdList;
    }

    /**
     * 即时库存导出查询条件
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class ExportSearchParamDTO extends SortDTO {

        /**
         * 勾选行数据（仅传该字段，其他字段不要传输）
         */
        private List<ExportInvParamDTO> checkData;

        /**
         * sku id编码集合，不提供给前端使用
         */
        @JsonIgnore
        private List<String> skuIdList;

        /**
         * sku编码
         */
        private List<String> skuNoList;


        /**
         * spu编码
         */
        private List<String> spuNoList;

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态集合 接口地址：plm/common/enumDropDown?type=SaleState
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织集合
         */
        private List<String> orgIdList;

        /**
         * 是否过滤0实际库存，默认前端页面勾上不显示0库存
         */
        private Boolean hideZeroInventory;

        /**
         * 查询维度：warehouse仓库，warehouseArea库区，warehouseLocation仓位
         */
        private String dimension;

        /**
         * 仓位编码
         */
//        private String warehouseLocationCode;

        /**
         * 库区编码
         */
//        private String warehouseAreaCode;

        /**
         * 库区编码集合
         */
        private List<String> warehouseAreaCodeList;

        /**
         * 仓位编码集合
         */
        private List<String> warehouseLocationCodeList;

        /**
         * 库区名称
         */
        private String warehouseAreaName;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 产品名称
         */
        private String productName;
    }

    /**
     * 即时库存勾选导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportInvParamDTO {

        /**
         * 仓库id（勾选导出必传参数）
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 库存组织id（勾选导出必传参数）
         */
        @NotEmpty(message = "库存组织不能为空")
        private String orgId;
        /**
         * 库存组织id（勾选导出必传参数）
         */
        @NotEmpty(message = "库存组织不能为空")
        private String id;

        /**
         * sku id（勾选导出必传参数）
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * 导出维度：warehouse，warehouseArea，warehouseLocation
         */
        @NotEmpty(message = "dimension不能为空")
        private String dimension;

        /**
         * 仓位编码（按仓位导出时传递）
         */
        private String warehouseAreaCode;

        /**
         * 库区编码（按库区导出时传递）
         */
        private String warehouseLocationCode;
    }


}
