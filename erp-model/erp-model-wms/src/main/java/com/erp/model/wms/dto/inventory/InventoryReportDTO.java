package com.erp.model.wms.dto.inventory;

import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 库存报表请求响应实体
 * @CreateTime: 2023-06-09  16:05
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class InventoryReportDTO implements Serializable {

    /**
     * 在途查询 查询条件
     */
    @Data
    @NoArgsConstructor
    public static class TransportSearchParamDTO extends SortDTO {

        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * spu 接口地址：/wms/drop/down/product/spuNo/list（一次性返回所有）
         */
        private List<String> spuNoList;

        /**
         * 仓库  接口地址：/wms/warehouse/list
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态 接口地址：/plm/common/enumDropDown?type=SaleState
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织 接口地址： /sys/company/list
         */
        private List<String> orgIdList;

    }

    /**
     * 在途查询导出  查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ExportTransportSearchParamDTO extends SortDTO {

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
         * spu 接口地址：/wms/drop/down/product/spuNo/list（一次性返回所有）
         */
        private List<String> spuNoList;

        /**
         * 仓库  接口地址：/wms/warehouse/list
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态 接口地址：/plm/common/enumDropDown?type=SaleState
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织 接口地址： /sys/company/list
         */
        private List<String> orgIdList;

        /**
         * 勾选的数据行
         */
        private List<ExportTransportItem> items;
    }

    /**
     * 在途查询 勾选导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportTransportItem {

        /**
         * 仓库id（勾选导出必传参数）
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * sku id（勾选导出必传参数）
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

    }

    /**
     * 在途查询 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class TransportPagingDTO {

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 产品图片链接
         */
        private String productImgUrl;

        /**
         * spu型号
         */
        private String spuNo;

        /**
         * 销售状态编码
         */
        private Integer saleState;

        /**
         * 销售状态名称
         */
        private String saleStateName;

        /**
         * 库存组织id
         */
        private String orgId;

        /**
         * 库存组织名称
         */
        private String orgName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 在途数量
         */
        private Integer transportQty;

        /**
         * 采购在途数量
         */
        private String purchaseQty;

        /**
         * 调拨在途数量
         */
        private Integer transferQty;

    }

    /**
     * 在途查询单据明细分页列表参数
     */
    @Data
    @NoArgsConstructor
    public static class ListTransportSearchParam {

        /**
         * 单据编号
         */
        private String sourceCode;

        /**
         * 单据类型 接口地址：/wms/common/enumDropDown?type=InventoryTransportType
         */
        private String sourceType;

        /**
         * 仓库id
         */
        @NotEmpty(message = "仓库id不能为空")
        private String warehouseId;

        /**
         * sku id
         */
        @NotEmpty(message = "sku id不能为空")
        private String skuId;

    }

    /**
     * 在途查询单据明细分页列表结果
     */
    @Data
    @NoArgsConstructor
    public static class ListTransportPagingDTO {

        /**
         * 业务日期
         */
        private LocalDate billDate;

        /**
         * 单据类型编码
         */
        private String sourceType;

        /**
         * 单据类型名称
         */
        private String sourceTypeName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 在途数量
         */
        private Integer transportQty;

        /**
         * 创建人用户id
         */
        private String createUserId;

        /**
         * 创建人用户名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

    }

    /**
     * 库龄计算表 查询条件
     */
    @Data
    @NoArgsConstructor
    public static class InventoryAgeSearchParamDTO extends SortDTO {

        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * spu 接口地址：/wms/drop/down/product/spuNo/list（一次性返回所有）
         */
        private List<String> spuNoList;

        /**
         * 仓库  接口地址：/wms/warehouse/list
         */
        private List<String> warehouseIdList;

        /**
         * 仓位 接口地址：/wms/warehouseLocation/list
         */
        private List<String> warehouseLocationList;

        /**
         * 库存组织 接口地址： /sys/company/list
         */
        private List<String> orgIdList;

    }

    /**
     * 库龄计算表分页列表结果
     */
    @Data
    @NoArgsConstructor
    public static class InventoryAgePagingDTO {

        /**
         * 标题
         */
        LinkedHashMap<String, String> head;

        /**
         * 结果集
         */
        List<LinkedHashMap<String, Object>> data;

    }

    /**
     * 库龄计算表 导出查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ExportInventoryAgeSearchParamDTO extends SortDTO {

        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * spu 接口地址：/wms/drop/down/product/spuNo/list（一次性返回所有）
         */
        private List<String> spuNoList;

        /**
         * 仓库  接口地址：/wms/warehouse/list
         */
        private List<String> warehouseIdList;

        /**
         * 仓位 接口地址：/wms/warehouseLocation/list
         */
        private List<String> warehouseLocationList;

        /**
         * 库存组织 接口地址： /sys/company/list
         */
        private List<String> orgIdList;

        /**
         * 勾选的数据行
         */
        private List<ExportInventoryAgeItem> items;

    }

    /**
     * 库龄计算表 勾选导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportInventoryAgeItem {

        /**
         * 仓库id（勾选导出必传参数）
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * sku id（勾选导出必传参数）
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

    }



}