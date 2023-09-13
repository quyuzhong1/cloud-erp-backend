package com.erp.model.bi.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * sku 目标设置表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@NoArgsConstructor
public class BiTargetSkuSettingDTO implements Serializable {




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
        * 主表id 对应 target_year 表id
        */
        private String mainId;

        /**
        * sku id
        */
        private String skuId;

        /**
        * sku no
        */
        private String skuNo;

        /**
        * 月
        */
        private Integer month;

        /**
        * 对应值
        */
        private BigDecimal value;

        /**
        * 指标维度
        */
        private String metrics;


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
        * 主表id 对应 target_year 表id
        */
        @NotBlank(message = "主表id 对应 target_year 表id不能为空")
        @Size(max = 19,message = "主表id 对应 target_year 表id最大长度不能超过19位")
        private String mainId;

        /**
        * sku id
        */
        @NotBlank(message = "sku id不能为空")
        @Size(max = 19,message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
        * sku no
        */
        @NotBlank(message = "sku no不能为空")
        @Size(max = 30,message = "sku no最大长度不能超过30位")
        private String skuNo;

        /**
        * 月
        */
        @NotNull(message = "月不能为空")
        private Integer month;

        /**
        * 对应值
        */
        @NotNull(message = "对应值不能为空")
        @Digits(integer = 12, fraction = 4, message = "对应值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal value;

        /**
        * 指标维度
        */
        @NotBlank(message = "指标维度不能为空")
        @Size(max = 20,message = "指标维度最大长度不能超过20位")
        private String metrics;


    }


}