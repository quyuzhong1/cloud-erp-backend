package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 店铺授权表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Data
@NoArgsConstructor
public class ShopAuthDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺登陆token
         */
        private String token;

        /**
         * 刷新的token
         */
        private String accessToken;

        /**
         * 过期时间
         */
        private LocalDate expiredTime;

        /**
         * 对应 dmp 表id
         */
        private String appClientId;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 店铺id
         */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19, message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
         * 店铺登陆token
         */
        @NotBlank(message = "店铺登陆token不能为空")
        @Size(max = 255, message = "店铺登陆token最大长度不能超过255位")
        private String token;

        /**
         * 刷新的token
         */
        @NotBlank(message = "刷新的token不能为空")
        @Size(max = 255, message = "刷新的token最大长度不能超过255位")
        private String accessToken;

        /**
         * 过期时间
         */
        private LocalDate expiredTime;

        /**
         * 对应 dmp 表id
         */
        @NotBlank(message = "对应 dmp 表id不能为空")
        @Size(max = 19, message = "对应 dmp 表id最大长度不能超过19位")
        private String appClientId;


    }

    @Data
    @NoArgsConstructor
    public static class ReturnDTO {
        /**
         * 记录id
         */
        @NotBlank(message = "店铺记录id不能为空")
        private String id;
        /**
         * 返回code
         */
        @NotBlank(message = "授权code不能为空")
        private String code;

        private Integer shopId;

        private Integer mainAccountId;
    }

}