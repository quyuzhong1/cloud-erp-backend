package com.erp.model.plm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: bom和sku分层分页查询
 * @date 2023/6/14 10:43
 */
@Data
@NoArgsConstructor
public class BomSkuPageDTO {

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * sku集合
         */
        private List<String> skuNoList;

        /**
         * 类目id
         */
        private List<String> categoryIdList;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {

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
        private String status;

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
         * 子件信息
         */
        private List<ListDTO> childList;
    }
}
