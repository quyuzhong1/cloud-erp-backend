package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 物流授权字段值表请求响应实体
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
*/
@Data
@NoArgsConstructor
public class LogisticsAuthFieldDTO implements Serializable {




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
        * 物流商授权表id
        */
        private String logisticsAuthId;

        /**
        * 字段
        */
        private String fieldCode;

        /**
         * 字段名
         */
        private String fieldName;

        /**
        * 字段值
        */
        private String fieldValue;


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
        * 字段
        */
        @NotBlank(message = "字段不能为空")
        @Size(max = 30,message = "字段最大长度不能超过30位")
        private String fieldCode;

        /**
        * 字段值
        */
        @NotBlank(message = "字段值不能为空")
        @Size(max = 100,message = "字段值最大长度不能超过100位")
        private String fieldValue;


    }


}