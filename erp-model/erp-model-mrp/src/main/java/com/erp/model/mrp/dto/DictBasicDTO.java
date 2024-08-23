package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 字典表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@NoArgsConstructor
public class DictBasicDTO implements Serializable {




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
        * 标识code 值
        */
        private String code;

        /**
        * 名称
        */
        private String name;

        /**
        * 类型
        */
        private String type;

        /**
        * 类型名称
        */
        private String typeName;

        private String remark;
        /**
         * 排序字段
         */
        private Integer index;

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
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 50,message = "名称最大长度不能超过50位")
        private String name;

        /**
        * 类型
        */
        @NotBlank(message = "类型不能为空")
        @Size(max = 32,message = "类型最大长度不能超过32位")
        private String type;

        /**
        * 类型名称
        */
        @NotBlank(message = "类型名称不能为空")
        @Size(max = 50,message = "类型名称最大长度不能超过50位")
        private String typeName;


    }


    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO {

        private String id;

        private String remark;

        private String code;

        private String type;

        private String typeName;

        private String name;



    }


}