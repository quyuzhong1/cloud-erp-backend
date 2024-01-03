package com.erp.model.dmp.dto;

import com.erp.model.dmp.entity.DmpShopInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 平台任务
 *
 * @Author Cloud
 * @Date 2023/8/24 18:03
 **/
public class PlatformTaskDTO {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddDTO{

        /**
         * 店铺id
         */
        @NotEmpty(message = "店铺id不能为空")
        private String shopId;

        private String shopName;
        /**
         * 平台编码
         */
        @NotEmpty(message = "平台编码不能为空")
        private String dictPlatform;

        /**
         * 平台店铺代号/平台卖家ID
         */
        private String platformShopCode;

        public AddDTO(String shopId, String shopName, String dictPlatform) {
            this.shopId = shopId;
            this.shopName = shopName;
            this.dictPlatform = dictPlatform;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisabledDTO{

        /**
         * 店铺id
         */
        @NotEmpty(message = "店铺id不能为空")
        private String shopId;
        /**
         * 平台编码
         */
        @NotEmpty(message = "平台编码不能为空")
        private String dictPlatform;

        @NotNull(message = "禁用状态不能为空")
        private Boolean disabled;

    }


}
