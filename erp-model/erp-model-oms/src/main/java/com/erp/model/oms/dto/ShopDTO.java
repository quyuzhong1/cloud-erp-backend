package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname ShopDTO
 * @Date 2023-06-28 18:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ShopDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO{


        /**
         * 平台
         */
        private String platformDict;

        /**
         * 店铺code
         */
        private String shopCode;

        /**
         * 店铺名称
         */
        private String name;

        /**
         * 客户的code
         */
        private String customerCode;

    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO{

         @NotBlank(message = "店铺表不能为空")
         private String id;

    }
}
