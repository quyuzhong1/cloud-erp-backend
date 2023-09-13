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
 * 产品便签关系表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@NoArgsConstructor
public class ProductRefLabelDTO implements Serializable {




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
        * 产品id
        */
        private String productId;

        /**
        * 标签id
        */
        private String leableId;


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
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19,message = "产品id最大长度不能超过19位")
        private String productId;

        /**
        * 标签id
        */
        @NotBlank(message = "标签id不能为空")
        @Size(max = 19,message = "标签id最大长度不能超过19位")
        private String leableId;


    }


}