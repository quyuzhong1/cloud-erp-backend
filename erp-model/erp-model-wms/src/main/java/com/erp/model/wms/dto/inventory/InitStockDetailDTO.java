package com.erp.model.wms.dto.inventory;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname: InventoryInitDetailDTO
 * @Description: TODO
 * @CreateTime: 2023-05-08  17:09
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class InitStockDetailDTO implements Serializable {

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * sku
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * sku
         */
        @NotEmpty(message = "sku不能为空")
        private String skuNo;

        /**
         * 期初数量
         */
        @NotNull(message = "期初数量不能为空")
        @Min(value = 0, message = "期初数量最小值为0")
        @Max(value = 999999999, message = "期初数量最大值为999999999")
        private Integer qty;


        @Size(max = 19, message = "仓位id长度不能超过19位")
        private String warehouseLocation;


        @Size(max = 255, message = "备注长度不能超过19位")
        private String remark;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 明细id
         */
        @NotEmpty(message = "id不能为空")
        private String id;

        /**
         * sku
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * sku
         */
        @NotEmpty(message = "sku不能为空")
        private String skuNo;

        /**
         * 期初数量
         */
        @NotNull(message = "期初数量不能为空")
        @Min(value = 0, message = "期初数量最小值为0")
        @Max(value = 999999999, message = "期初数量最大值为999999999")
        private Integer qty;


        @Size(max = 19, message = "仓位id长度不能超过19位")
        private String warehouseLocation;


        @Size(max = 255, message = "备注长度不能超过19位")
        private String remark;

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 明细id
         */
        private String id;

        /**
         * 主单id
         */
        private String mainId;

        /**
         * sku
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 期初数量
         */
        private Integer qty;

        /**
         * 库位id
         */
        private String warehouseLocation;

        /**
         * 库位名称
         */
        private String warehouseLocationName;

        /**
         * 备注
         */
        private String remark;

    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<InitStockDetailDTO.AddDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }

}