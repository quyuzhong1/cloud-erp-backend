package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/10 14:11
 */
@Data
@NoArgsConstructor
public class TransferApplicationDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;
        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        @Min(value = 1,message = "数量最小值为1")
        @Max(value = 99999999,message = "数量最大值为99999999")
        private Integer qty;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "明细主键id不能为空")
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends UpdateDTO {

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 及时库存
         */
        private Integer curInventoryQty;

    }

}
