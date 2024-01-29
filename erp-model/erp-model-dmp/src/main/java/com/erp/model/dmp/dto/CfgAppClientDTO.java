package com.erp.model.dmp.dto;

import com.erp.model.dmp.enums.AppClientEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 第三方应用程序信息表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Data
@NoArgsConstructor
public class CfgAppClientDTO implements Serializable {


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
         * 平台类型 如消息平台，销售平台
         */
        private String platformType;

        /**
         * 对应平台 如 微信 亚马逊
         */
        private String dictPlatform;

        /**
         * 业务类型如店铺授权
         */
        private String businessType;

        /**
         * 客户端id
         */
        private String clientId;

        /**
         * 客户端secret
         */
        private String clientSecret;

        /**
         * 对应的url
         */
        private String url;

        /**
         * 回调的url
         */
        private String redirectUrl;


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
         * 平台类型 如消息平台，销售平台
         */
        @NotBlank(message = "平台类型 如消息平台，销售平台不能为空")
        @Size(max = 30, message = "平台类型 如消息平台，销售平台最大长度不能超过30位")
        private String platformType;

        /**
         * 对应平台 如 微信 亚马逊
         */
        @NotBlank(message = "对应平台 如 微信 亚马逊不能为空")
        @Size(max = 30, message = "对应平台 如 微信 亚马逊最大长度不能超过30位")
        private String dictPlatform;

        /**
         * 业务类型如店铺授权
         */
        @NotBlank(message = "业务类型如店铺授权不能为空")
        @Size(max = 30, message = "业务类型如店铺授权最大长度不能超过30位")
        private String businessType;

        /**
         * 客户端id
         */
        @NotBlank(message = "客户端id不能为空")
        @Size(max = 50, message = "客户端id最大长度不能超过50位")
        private String clientId;

        /**
         * 客户端secret
         */
        @NotBlank(message = "客户端secret不能为空")
        @Size(max = 50, message = "客户端secret最大长度不能超过50位")
        private String clientSecret;

        /**
         * 对应的url
         */
        @NotBlank(message = "对应的url不能为空")
        @Size(max = 500, message = "对应的url最大长度不能超过500位")
        private String url;

        /**
         * 回调的url
         */
        @NotBlank(message = "回调的url不能为空")
        @Size(max = 500, message = "回调的url最大长度不能超过500位")
        private String redirectUrl;


    }

    /**
     * 查找
     */
    @Data
    @NoArgsConstructor
    public static class FindDTO {

        /**
         * 平台类型
         */
        private String platformType;

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 业务类型
         */
        private String businessType;


        public static FindDTO init(AppClientEnum appClient) {
            FindDTO findDTO = new FindDTO();
            findDTO.setBusinessType(appClient.getBusinessType());
            findDTO.setDictPlatform(appClient.getPlatform());
            findDTO.setPlatformType(appClient.getPlatformType());
            return findDTO;
        }
    }


}