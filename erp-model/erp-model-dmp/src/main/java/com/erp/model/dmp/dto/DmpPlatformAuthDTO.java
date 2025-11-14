package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 平台token授权表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-08-28
*/
@Data
@NoArgsConstructor
public class DmpPlatformAuthDTO implements Serializable {




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
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 登陆token
        */
        private String token;

        /**
        * 授权的token
        */
        private String accessToken;

        /**
        * 中台cfg_app_client的id
        */
        private String appClientId;

        /**
        * 访问令牌过期之前的秒数
        */
        private Integer expiresIn;

        /**
        * 刷新token
        */
        private String refreshToken;

        /**
        * token失效时间（不是平台标准的失效时间，要存往前推提前刷新的时间，不能失败了再刷新）
        */
        private LocalDateTime expireTime;


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
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        @NotBlank(message = "唯一字段md5值不能为空")
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        @NotBlank(message = "数据字段md5值不能为空")
        private String dataEncrypt;

        /**
        * 登陆token
        */
        @NotBlank(message = "登陆token不能为空")
        private String token;

        /**
        * 授权的token
        */
        @NotBlank(message = "授权的token不能为空")
        private String accessToken;

        /**
        * 中台cfg_app_client的id
        */
        @NotBlank(message = "中台cfg_app_client的id不能为空")
        @Size(max = 19,message = "中台cfg_app_client的id最大长度不能超过19位")
        private String appClientId;

        /**
        * 访问令牌过期之前的秒数
        */
        @NotNull(message = "访问令牌过期之前的秒数不能为空")
        private Integer expiresIn;

        /**
        * 刷新token
        */
        @NotBlank(message = "刷新token不能为空")
        private String refreshToken;

        /**
        * token失效时间（不是平台标准的失效时间，要存往前推提前刷新的时间，不能失败了再刷新）
        */
        private LocalDateTime expireTime;


    }


}