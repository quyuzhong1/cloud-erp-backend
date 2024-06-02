package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 远程搜索
 *
 * @author hyj
 * @date 2024/6/2 17:24
 */

@Data
@NoArgsConstructor
public class UserSelectDto {
    @Data
    @NoArgsConstructor
    public static class PageSelectDTO {
        /**
         * 用户id
         */
        private String userId;

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 用户状态1：正常 0：禁用
         */
        private Integer userState;

        /**
         * 是否自己
         */
        private Integer isMyState;

        /**
         * 是否禁用 true 禁用
         */
        private Boolean disabled;
    }

    /**
     * 远程搜索
     */
    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;
    }

}
