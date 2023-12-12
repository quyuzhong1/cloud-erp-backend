package com.erp.model.dmp.dto;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 第三方仓任务
 *
 **/
public class ThirdWarehouseTaskDTO {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddDTO{

        /**
         * 授权表主键
         */
        @NotEmpty(message = "授权表主键不能为空")
        private String authKey;

        /**
         * 授权信息
         */
        @NotEmpty(message = "授权信息不能为空")
        private Map<String, Object> authInfo;

        /**
         * 平台编码
         */
        @NotEmpty(message = "平台编码不能为空")
        private String dictPlatform;
    }

}
