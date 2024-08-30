package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 标签信息表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-30
*/
@Data
@NoArgsConstructor
public class LabelInfoDTO implements Serializable {




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
        * 标签名字
        */
        private String name;

        /**
        * 颜色
        */
        private String color;

        /**
        * 是否禁用
        */
        private Boolean disabled;


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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 标签名字
        */
        @NotBlank(message = "标签名字不能为空")
        @Size(max = 255,message = "标签名字最大长度不能超过255位")
        private String name;

        /**
        * 颜色
        */
        @NotBlank(message = "颜色不能为空")
        @Size(max = 255,message = "颜色最大长度不能超过255位")
        private String color;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

    }


}