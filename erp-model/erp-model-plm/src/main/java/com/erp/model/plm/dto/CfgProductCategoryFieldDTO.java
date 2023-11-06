package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 产品分类字段配置表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-06
*/
@Data
@NoArgsConstructor
public class CfgProductCategoryFieldDTO implements Serializable {




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
        * 分类id
        */
        private String categoryId;

        /**
        * 字段
        */
        private String fieldCode;

        /**
        * 字段名
        */
        private String fieldName;


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
        * 分类id
        */
        @NotBlank(message = "分类id不能为空")
        @Size(max = 19,message = "分类id最大长度不能超过19位")
        private String categoryId;

        /**
        * 字段
        */
        @NotBlank(message = "字段不能为空")
        @Size(max = 30,message = "字段最大长度不能超过30位")
        private String fieldCode;

        /**
        * 字段名
        */
        @NotBlank(message = "字段名不能为空")
        @Size(max = 50,message = "字段名最大长度不能超过50位")
        private String fieldName;


    }


}