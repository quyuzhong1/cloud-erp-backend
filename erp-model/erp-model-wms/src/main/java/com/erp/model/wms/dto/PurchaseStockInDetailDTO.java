package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/13 10:25
 */
@Data
@NoArgsConstructor
public class PurchaseStockInDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 实收数量
         */
        @NotNull(message = "实收数量不能为空")
        @Min(value = 1,message = "实收数量最小值为1")
        @Max(value = 99999999,message = "实收数量最大值为99999999")
        private Integer stockInQty;

        /**
         * 超收数量
         */
        @NotNull(message = "超收数量不能为空")
        @Min(value = 0,message = "超收数量最小值为0")
        @Max(value = 99999999,message = "超收数量最大值为99999999")
        private Integer exceedQty;

        /**
         * 库位id
         */
        private String warehouseLocationId;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;

        /**
         * 来源明细id
         */
        @NotBlank(message = "来源明细id不能为空")
        private String sourceDetailId;

        /**
         * 采购明细id
         */
        @NotBlank(message = "采购明细id不能为空")
        private String purchaseOrderDetailId;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends  AddDTO{
        /**
         * 主键id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends  UpdateDTO{
        /**
         * 未入库数量
         */
       private Integer unStockInQty;
    }
}
