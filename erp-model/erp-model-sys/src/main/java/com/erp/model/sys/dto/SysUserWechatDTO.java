package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 微信用户表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
*/
@Data
@NoArgsConstructor
public class SysUserWechatDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * sysUid
        */
        private String uid;

        /**
        * 用于关联同一开放平台下的多个应用的用户
        */
        private String unionId;

        /**
        * 微信用户在应用中的唯一标识
        */
        private String openId;

        /**
        * 微信名称
        */
        private String nickName;

        /**
        * 微信用户的头像URL
        */
        private String avatarUrl;

        /**
        * 性别
        */
        private String gender;

        /**
        * 上次登录时间
        */
        private LocalDateTime lastLoginTime;


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
        * sysUid
        */
        @NotBlank(message = "sysUid不能为空")
        @Size(max = 64,message = "sysUid最大长度不能超过64位")
        private String uid;

        /**
        * 用于关联同一开放平台下的多个应用的用户
        */
        @NotBlank(message = "用于关联同一开放平台下的多个应用的用户不能为空")
        @Size(max = 255,message = "用于关联同一开放平台下的多个应用的用户最大长度不能超过255位")
        private String unionId;

        /**
        * 微信用户在应用中的唯一标识
        */
        @NotBlank(message = "微信用户在应用中的唯一标识不能为空")
        @Size(max = 255,message = "微信用户在应用中的唯一标识最大长度不能超过255位")
        private String openId;

        /**
        * 微信名称
        */
        @NotBlank(message = "微信名称不能为空")
        @Size(max = 100,message = "微信名称最大长度不能超过100位")
        private String nickName;

        /**
        * 微信用户的头像URL
        */
        private String avatarUrl;

        /**
        * 性别
        */
        private String gender;

        /**
        * 上次登录时间
        */
        private LocalDateTime lastLoginTime;


    }


}