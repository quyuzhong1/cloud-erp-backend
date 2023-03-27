package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 17:36
 */
@Data
@NoArgsConstructor
public class SalesDemandDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 主表id
         */
        private String salesDemandId;

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 单箱数量
         */
        @Min(value = 0,message = "计划备货数量最小值为0")
        @Max(value = 99999999,message = "计划备货数量最大值为99999999")
        private Integer unitQty;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 计划交期
         */
        private LocalDate planDeliveryDate;

        /**
         * 计划备货数量
         */
        @NotNull(message = "计划备货数量不能为空")
        @Min(value = 1,message = "计划备货数量最小值为1")
        @Max(value = 99999999,message = "计划备货数量最大值为99999999")
        private Integer planStockQty;

        /**
         * 目的仓库id
         */
        @NotBlank(message = "目的仓库不能为空")
        private String destWarehouseId;

        /**
         * 目的仓库名称
         */
        private String destWarehouseName;

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
         * 主表id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
       /**
        * 成功返回数据
        */
       private List<AddDTO> successList;

       /**
        * 错误url
        */
       private String errorUrl;
    }


}
