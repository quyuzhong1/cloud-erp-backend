package com.erp.model.plm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 产品应用分类请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2025-01-09
*/
@Getter
@Setter
public class ApplicationCategoryDTO implements Serializable {




    /**
    * 详情
    */
    @Getter
    @Setter
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 分类名
        */
        private String name;

        /**
        * 分类代码
        */
        private String code;


    }

    /**
    * 新增
    */
    @Getter
    @Setter
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Getter
    @Setter
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
        * 分类名
        */
        @NotBlank(message = "分类名不能为空")
        @Size(max = 20,message = "分类名最大长度不能超过20位")
        private String name;

        /**
         * 分类名
         */
        @NotBlank(message = "分类代码不能为空")
        @Size(max = 4,message = "分类代码最大长度不能超过4位")
        @Pattern(regexp = "^[A-Z]+$", message = "只能包含大写字母")
        private String code;
    }


}