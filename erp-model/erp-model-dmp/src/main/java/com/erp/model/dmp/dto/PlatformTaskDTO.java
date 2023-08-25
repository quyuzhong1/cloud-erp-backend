package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * 平台任务
 *
 * @Author Cloud
 * @Date 2023/8/24 18:03
 **/
public class PlatformTaskDTO {


    @Data
    @NoArgsConstructor
    public static class AddDTO{

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

    }


}
