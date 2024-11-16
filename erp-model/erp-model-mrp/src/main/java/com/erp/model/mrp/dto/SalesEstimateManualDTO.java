package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 运营销量预估请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-09-05
*/
@Data
@NoArgsConstructor
public class SalesEstimateManualDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 补货建议id
        */
        private String replenishmentId;

        /**
        * 当月销量预估
        */
        private BigDecimal currentMonthSalesQty;

        /**
        * 当月销量剩余预估
        */
        private BigDecimal currentMonthSurplusSalesQty;

        /**
        * 下月销量预估
        */
        private BigDecimal nextMonthSales;

        /**
        * 后月销量预估
        */
        private BigDecimal followingMonthSales;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 补货建议id
        */
        @NotBlank(message = "补货建议id不能为空")
        @Size(max = 19,message = "补货建议id最大长度不能超过19位")
        private String replenishmentId;

        /**
        * 当月销量预估
        */
        @NotNull(message = "当月销量预估不能为空")
        private BigDecimal currentMonthSalesQty;

        /**
        * 当月销量剩余预估
        */
        @NotNull(message = "当月销量剩余预估不能为空")
        private BigDecimal currentMonthSurplusSalesQty;

        /**
        * 下月销量预估
        */
        @NotNull(message = "下月销量预估不能为空")
        private BigDecimal nextMonthSales;

        /**
        * 后月销量预估
        */
        @NotNull(message = "后月销量预估不能为空")
        private BigDecimal followingMonthSales;


    }


}