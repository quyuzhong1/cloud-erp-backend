package com.erp.model.plm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: bom和sku分层分页查询
 * @date 2023/6/14 10:43
 */
@Data
@NoArgsConstructor
public class BomSkuPageDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * sku集合
         */
        private List<String> skuNoList;

        /**
         * 远程搜索sku
         */
        private String remoteSearchSku;

        /**
         * 类目id
         */
        private List<String> categoryIdList;
        /**
         * 产品属性
         */
        private List<String> notPropertyList;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 序号
         */
        private Integer index;

        /**
         * bomId
         */
        private String bomId;

        /**
         * 图片
         */
        private String imagesUrl;

        /**
         * SKU名称
         */
        private String skuName;

        /**
         * SKUID
         */
        private String skuId;

        /**
         * SKU编码
         */
        private String skuNo;

        /**
         * 产品id
         */
        private String productId;

        /**
         * spu编号
         */
        private String spuNo;

        /**
         * SKU状态
         */
        private Integer status;

        /**
         * SKU状态名称
         */
        private String statusName;

        /**
         * 二级分类
         */
        private String category;

        /**
         * 产品品类
         */
        private String brandName;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 付款条件
         */
        private String paymentCondition;

        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * 变体属性
         */
        private String variantProperty;

        /**
         * 子件信息
         */
        private List<ChildDTO> childList;
    }

    @Data
    @NoArgsConstructor
    public static class ChildDTO {

        /**
         * 序号
         */
        private Integer index;

        /**
         * bomId
         */
        private String bomId;

        /**
         * 图片
         */
        private String imagesUrl;

        /**
         * SKU名称
         */
        private String skuName;

        /**
         * SKUID
         */
        private String skuId;

        /**
         * SKU编码
         */
        private String skuNo;

        /**
         * 产品id
         */
        private String productId;

        /**
         * spu编号
         */
        private String spuNo;

        /**
         * SKU状态
         */
        private Integer status;

        /**
         * SKU状态名称
         */
        private String statusName;

        /**
         * 二级分类
         */
        private String category;

        /**
         * 产品品类
         */
        private String brandName;

        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * 用量
         */
        private Integer quantity;

        /**
         * 变体属性
         */
        private String variantProperty;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 付款条件
         */
        private String paymentCondition;
    }

    @Data
    @NoArgsConstructor
    public static class AllSkuParamDTO {

        /**
         * skuId集合
         */
        private List<String> skuIdList;

        /**
         * sku编码集合
         */
        private List<String> skuNoList;
    }

    @Data
    @NoArgsConstructor
    public static class ListAllSkuDTO {

        /**
         * 父级SKU
         */
        private List<ListSkuLevelDTO> parentList;

        /**
         * 子级SKU
         */
        private List<ListSkuLevelDTO> childList;
    }


    @Data
    @NoArgsConstructor
    public static class ListSkuLevelDTO {

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 父级skuId
         */
        private String parentSkuId;

        /**
         * 父级sku编号
         */
        private String parentSkuNo;

        /**
         * 用量
         */
        private Integer quantity;
    }
}
