package com.erp.model.wms.dto.inventory;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * @Classname: InstockForcastDetailDTO
 * @Description: TODO
 * @CreateTime: 2023-05-09  15:34
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class InstockForcastDetailDTO implements Serializable {

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
         * 采购订单明细id
         */
        @NotEmpty(message = "采购订单明细id不能为空")
        private String purchaseOrderDetailId;

        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        @Min(value = 0, message = "数量最小值为0")
        @Max(value = 999999999, message = "数量最大值为999999999")
        private Integer qty;

        @Size(max = 50, message = "备注长度不能超过19位")
        private String productName;

    }

}