package com.erp.model.mrp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 试算销量去噪请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@NoArgsConstructor
public class CalcSalesInfoDenoisingDTO implements Serializable {




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
        * 试算id
        */
        private String calcSalesInfoDimId;

        /**
        * 日期
        */
        private LocalDateTime date;

        /**
        * 销量
        */
        private BigDecimal qty;

        /**
        * 去噪类型，percentage百分比去噪，fixedValue固定值去噪，completely完全去噪
        */
        private String denoisingType;


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
        * 试算id
        */
        @NotBlank(message = "试算id不能为空")
        @Size(max = 19,message = "试算id最大长度不能超过19位")
        private String calcSalesInfoDimId;

        /**
        * 日期
        */
        @NotNull(message = "日期不能为空")
        private LocalDateTime date;

        /**
        * 销量
        */
        @NotNull(message = "销量不能为空")
        @Digits(integer = 14, fraction = 2, message = "销量整数位不能超过14位，小数位不能超过2位")
        private BigDecimal qty;

        /**
        * 去噪类型，percentage百分比去噪，fixedValue固定值去噪，completely完全去噪
        */
        @NotBlank(message = "去噪类型，percentage百分比去噪，fixedValue固定值去噪，completely完全去噪不能为空")
        @Size(max = 255,message = "去噪类型，percentage百分比去噪，fixedValue固定值去噪，completely完全去噪最大长度不能超过255位")
        private String denoisingType;


    }


}