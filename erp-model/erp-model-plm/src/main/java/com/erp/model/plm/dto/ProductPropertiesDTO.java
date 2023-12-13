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
 * sku与配置字段关系表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-08
*/
@Data
@NoArgsConstructor
public class ProductPropertiesDTO implements Serializable {




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
        * sku id
        */
        private String skuId;

        /**
        * 字段值
        */
        private String fieldValue;

        /**
        * 字段
        */
        private String fieldCode;


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
        * sku id
        */
        @NotBlank(message = "sku id不能为空")
        @Size(max = 19,message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
        * 字段值
        */
        @NotBlank(message = "字段值不能为空")
        @Size(max = 30,message = "字段值最大长度不能超过30位")
        private String fieldValue;

        /**
        * 字段
        */
        @NotBlank(message = "字段不能为空")
        @Size(max = 30,message = "字段最大长度不能超过30位")
        private String fieldCode;


    }


}