package com.erp.model.wms.dto.inventory;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * @Classname: InventoryFinishDeliveryDetailDTO
 * @Description: TODO
 * @CreateTime: 2023-05-10  17:15
 * @Author: zhangchunlin
 */
@Data
public class InventoryFinishDeliveryDetailDTO implements Serializable {

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
         * 数量
         * 此处需注意：采购订单剩余未交货数量
         */
        @NotNull(message = "数量不能为空")
        @Min(value = 0, message = "数量最小值为0")
        @Max(value = 999999999, message = "数量最大值为999999999")
        private Integer qty;


    }

}