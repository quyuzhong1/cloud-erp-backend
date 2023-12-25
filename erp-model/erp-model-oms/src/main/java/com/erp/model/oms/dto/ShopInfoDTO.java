package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * 店铺信息请求响应实体
 * </p>
 *
 * @author JIm
 * @since 2023-12-25
 */
@Data
@NoArgsConstructor
public class ShopInfoDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class RelatedDTO {
        /**
         * 店铺id
         */
        @NotBlank(message = "店铺id不能为空")
        private String shopId;
        /**
         * country
         */
        @NotBlank(message = "country不能为空")
        private String country;
    }

}