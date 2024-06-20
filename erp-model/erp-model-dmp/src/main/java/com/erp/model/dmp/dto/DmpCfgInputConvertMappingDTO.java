package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 转换映射请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-20
*/
@Data
@NoArgsConstructor
public class DmpCfgInputConvertMappingDTO implements Serializable {




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
        * 原始键
        */
        private String originalKey;

        /**
        * 转换键
        */
        private String convertKey;

        /**
        * 映射类型
        */
        private String mappingStatus;


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
        * 原始键
        */
        @NotBlank(message = "原始键不能为空")
        @Size(max = 255,message = "原始键最大长度不能超过255位")
        private String originalKey;

        /**
        * 转换键
        */
        @NotBlank(message = "转换键不能为空")
        @Size(max = 255,message = "转换键最大长度不能超过255位")
        private String convertKey;

        /**
        * 映射类型
        */
        @NotBlank(message = "映射类型不能为空")
        @Size(max = 255,message = "映射类型最大长度不能超过255位")
        private String mappingStatus;


    }


}