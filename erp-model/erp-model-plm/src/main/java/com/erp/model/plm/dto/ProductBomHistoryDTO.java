package com.erp.model.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 历史BOMDTO
 * @date 2023/8/28 18:58
 */
@Data
@NoArgsConstructor
public class ProductBomHistoryDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ParamDTO {

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VersionDTO {
        /**
         * bom版本
         */
        private String bomVersion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BomHistoryQueryDTO {
        /**
         * BOM 历史 id 集合
         */
        private List<String> bomHistoryIds;

        /**
         * 组合品父 SKU id 集合
         */
        private List<String> parentSkuIds;
    }

}
