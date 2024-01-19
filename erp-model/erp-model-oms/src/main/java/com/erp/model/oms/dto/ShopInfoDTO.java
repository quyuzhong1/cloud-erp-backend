package com.erp.model.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListParamDTO {
        /**
         * 授权状态
         */
        @NotBlank(message = "授权状态不能为空")
        private String authStatus;
        /**
         * 平台类型
         */
        @NotBlank(message = "dictPlatform不能为空")
        private String dictPlatform;

        /**
         * 店铺IDS
         */
        private List<String> shopIdList;

    }

}