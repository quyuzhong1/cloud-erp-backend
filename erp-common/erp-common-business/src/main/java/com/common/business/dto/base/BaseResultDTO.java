package com.common.business.dto.base;

import com.baomidou.mybatisplus.annotation.TableField;
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
    public static class AddDTO {
        /**
         * 表 id
         */
        private String id;

        /**
         * 编码
         */
        private String code;

        public AddDTO(String id, String code) {
            this.id = id;
            this.code = code;
        }
    }

    /**
     * 修改返回值
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 表 id
         */
        private String id;

        /**
         * 编码
         */
        private String code;

        public UpdateDTO(String id, String code) {
            this.id = id;
            this.code = code;
        }
    }
}
