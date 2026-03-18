package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: 组合产品DTO
 * @date 2023/8/16 9:40
 */
@Data
@NoArgsConstructor
public class BomCombinationDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量最小值为1")
        @Max(value = 999999999, message = "数量最大值为999999999")
        private Integer qty;
    }


    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO{


    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO{

        /**
         * 主键id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO{

        /**
         * 主键id
         */
        private String id;
        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;
        
        /**
         * 标准零售价(含税)
         */
        private BigDecimal stdRetailPriceVat;
    }
}
