package com.erp.model.dmp.dto;

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
 * 分类 目标设置表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@NoArgsConstructor
public class BiTargetCategorySettingDTO implements Serializable {




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
        * 分类id
        */
        private String categoryId;

        /**
        * 分类名
        */
        private String categoryName;

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
        * 分类id
        */
        @NotBlank(message = "分类id不能为空")
        @Size(max = 19,message = "分类id最大长度不能超过19位")
        private String categoryId;

        /**
        * 分类名
        */
        @NotBlank(message = "分类名不能为空")
        @Size(max = 30,message = "分类名最大长度不能超过30位")
        private String categoryName;

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