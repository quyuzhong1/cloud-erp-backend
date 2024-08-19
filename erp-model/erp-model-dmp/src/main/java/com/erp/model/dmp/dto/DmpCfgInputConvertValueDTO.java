package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-08
*/
@Data
@NoArgsConstructor
public class DmpCfgInputConvertValueDTO implements Serializable {




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
        * 映射dmp_cfg_input_convert_mapping表id
        */
        private String mainId;

        /**
        * 转换后的值
        */
        private String convertAfterValue;

        /**
        * 转换前的值
        */
        private String convertBeforeValue;


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
        * 映射dmp_cfg_input_convert_mapping表id
        */
        @NotBlank(message = "映射dmp_cfg_input_convert_mapping表id不能为空")
        @Size(max = 255,message = "映射dmp_cfg_input_convert_mapping表id最大长度不能超过255位")
        private String mainId;

        /**
        * 转换后的值
        */
        @NotBlank(message = "转换后的值不能为空")
        @Size(max = 255,message = "转换后的值最大长度不能超过255位")
        private String convertAfterValue;

        /**
        * 转换前的值
        */
        @NotBlank(message = "转换前的值不能为空")
        @Size(max = 255,message = "转换前的值最大长度不能超过255位")
        private String convertBeforeValue;


    }



    @Data
    @NoArgsConstructor
    public static class MappingAndValueDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 映射dmp_cfg_input_convert_mapping表id
         */
        private String mainId;

        /**
         * dmp_cfg_input_convert_mapping表id
         */
        private String convertId;

        /**
         * 原始键
         */
        private String originalKey;

        /**
         * 转换键
         */
        private String convertKey;


        /**
         * 转换后的值
         */
        private String convertAfterValue;

        /**
         * 转换前的值
         */
        private String convertBeforeValue;
    }


}