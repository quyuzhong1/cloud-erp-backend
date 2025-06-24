package com.common.business.dto.base;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

    /**
     * 操作结果展示
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentDTO {
        /**
         * 数据id
         */
        private String id;
        /**
         * 操作单号
         */
        private String code;
        /**
         * 操作提示
         */
        private String msg;
        /**
         * 操作结果
         */
        private String content;
    }


    /**
     * 操作结果展示
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddAndSubmmitDTO {
        /**
         * 表 id
         */
        private String id;

        /**
         * 编码
         */
        private String code;

        /**
         * 新增或编辑页面，是否关闭当前页面
         */
        private Boolean isClose = Boolean.TRUE;
    }
}
