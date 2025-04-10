package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-04-03
*/
@Data
@NoArgsConstructor
public class ThridUserInfoDTO implements Serializable {




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
        * unionId
        */
        private String unionid;

        /**
        * openId
        */
        private String openid;

        /**
        * 用户昵称
        */
        private String nickName;

        /**
        * 用户头像图片的 URL
        */
        private String avatarUrl;

        /**
        * 用户性别
        */
        private Integer gender;

        /**
        * 姓名
        */
        private String username;

        /**
        * 手机号码
        */
        private String phoneNumber;

        /**
        * 所在国家
        */
        private String country;

        /**
        * 所在省份
        */
        private String province;

        /**
        * 所在城市
        */
        private String city;

        /**
        * 显示 country，province，city 所用的语言,强制返回 “zh_CN”
        */
        private String language;

        /**
        * 最近登录时间
        */
        private LocalDateTime lastLoginTime;

        /**
        * 用户状态
        */
        private String status;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

//        /**
//         * 用户登录凭证（有效期五分钟）。开发者需要在开发者服务器后台调用 code2Session，使用 code 换取 openid、unionid、session_key 等信息
//         */
//        private String jsCode;


        private String thirdUserId;


        /**
         * 用户对象信息
         */
        private UserInfo userInfo;

        /**
         *不包括敏感信息的原始数据字符串，用于计算签名
         */
        private String rawData;

        /**
         *使用 sha1( rawData + sessionkey ) 得到字符串，用于校验用户信息，详见 用户数据的签名验证和加解密
         */
        private String signature;

        /**
         *包括敏感数据在内的完整用户信息的加密数据，详见 用户数据的签名验证和加解密
         */
        private String encryptedData;

        /**
         *加密算法的初始向量，详见 用户数据的签名验证和加解密
         */
        private String iv;

        /**
         *敏感数据对应的云 ID，开通云开发的小程序才会返回，可通过云调用直接获取开放数据，详细见云调用直接获取开放数据
         */
        private String cloudID;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class CodeToSessionDTO {

        /**
         * 用户登录凭证（有效期五分钟）。开发者需要在开发者服务器后台调用 code2Session，使用 code 换取 openid、unionid、session_key 等信息
         */
        @NotBlank(message = "jsCode不能为空")
        private String jsCode;

        @NotBlank(message = "thirdUserId不能为空")
        private String thirdUserId;
    }

    /**
     * 用户对象信息
     */
    @Data
    @NoArgsConstructor
    public static class UserInfo {


        /**
         * 用户昵称
         */
        private String nickName;

        /**
         * 用户头像图片的 URL
         */
        private String avatarUrl;

        /**
         * 用户性别
         */
        private Integer gender;

        /**
         * 所在国家
         */
        private String country;

        /**
         * 所在省份
         */
        private String province;

        /**
         * 所在城市
         */
        private String city;

        /**
         * 显示 country，province，city 所用的语言,强制返回 “zh_CN”
         */
        private String language;



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
        * unionId
        */
        @Size(max = 64,message = "unionId最大长度不能超过64位")
        private String unionid;

        /**
        * openId
        */
        @Size(max = 64,message = "openId最大长度不能超过64位")
        private String openid;

        /**
        * 用户昵称
        */
        @Size(max = 64,message = "用户昵称最大长度不能超过64位")
        private String nickName;

        /**
        * 用户头像图片的 URL
        */
        @Size(max = 64,message = "用户头像图片的 URL最大长度不能超过64位")
        private String avatarUrl;

        /**
        * 用户性别
        */
        private Integer gender;

        /**
        * 姓名
        */
        @NotBlank(message = "姓名不能为空")
        @Size(max = 64,message = "姓名最大长度不能超过64位")
        private String username;

        /**
        * 手机号码
        */
        @NotBlank(message = "手机号码不能为空")
        @Size(max = 32,message = "手机号码最大长度不能超过32位")
        private String phoneNumber;

        /**
        * 所在国家
        */
        @Size(max = 32,message = "所在国家最大长度不能超过32位")
        private String country;

        /**
        * 所在省份
        */
        @Size(max = 32,message = "所在省份最大长度不能超过32位")
        private String province;

        /**
        * 所在城市
        */
        @Size(max = 32,message = "所在城市最大长度不能超过32位")
        private String city;

        /**
        * 显示 country，province，city 所用的语言,强制返回 “zh_CN”
        */
        private String language;

        /**
        * 最近登录时间
        */
        private LocalDateTime lastLoginTime;

        /**
        * 用户状态
        */
        private String status;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class CodeToSessionResp {

        private String sessionKey;

        private String unionid;

        private String errmsg;

        private String openid;

        private Integer errcode;
    }



}