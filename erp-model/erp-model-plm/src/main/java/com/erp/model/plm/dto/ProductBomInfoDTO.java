package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
public class ProductBomInfoDTO {
    /**
     * 查询sku版本返回值
     */
    @Data
    @NoArgsConstructor
    public static class skuBomVersion {
        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 版本
         */
        private List<String> bomVersionList;
    }
    /**
     * 查询sku版本查询参数
     */
    @Data
    @NoArgsConstructor
    public static class skuBomVersionParams{
        /**
         * sku编码
         */
        private List<String> skuNos;
    }

    @Data
    @NoArgsConstructor
    public static class skuIdParams {
        /**
         * 产品id
         */
        @NotNull(message = "产品id不能为空")
        private List<String> skuIds;
    }
}
