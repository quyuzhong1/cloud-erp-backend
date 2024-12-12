package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * B2C销售订单分类表请求响应实体
 *
 * @author Will
 * @since 2023-08-18
*/
@Data
@NoArgsConstructor
public class SoB2cRefCategoryDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO{

        /**
        * 主键id
        */
        private String  id;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


        public AddDTO (String soB2cId, String categoryId) {
            super.setSoB2cId(soB2cId);
            super.setCategoryId(categoryId);
        }
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
        * b2c销售订单id
        */
        @NotBlank(message = "b2c销售订单id不能为空")
        @Size(max = 19,message = "b2c销售订单id最大长度不能超过19位")
        private String soB2cId;

        /**
        * 分类id
        */
        @NotBlank(message = "分类id不能为空")
        @Size(max = 19,message = "分类id最大长度不能超过19位")
        private String categoryId;
    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class CategoryNamesDTO  {

        private String soB2cId;

        private String categoryNames;

    }

}