package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回参数
 * @Author Luo_WG
 * @Date 2023/10/30 10:21
 **/
public class BaseResultDTO implements Serializable {

    /**
     * 新增返回值
     */
    @Data
    @NoArgsConstructor
    public static class addDTO {
        /**
         * 表 id
         */
        private String id;

        /**
         * 编码
         */
        private String code;
    }

    /**
     * 修改返回值
     */
    @Data
    @NoArgsConstructor
    public static class updateDTO {
        /**
         * 表 id
         */
        private String id;

        /**
         * 编码
         */
        private String code;
    }
}
