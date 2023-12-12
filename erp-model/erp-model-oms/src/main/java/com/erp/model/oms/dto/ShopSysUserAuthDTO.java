package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 店铺权限设置表请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-09-01
*/
@Data
@NoArgsConstructor
public class ShopSysUserAuthDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 用户id
        */
        private String userId;

        /**
        * 授权类型（all全部授权，part指定授权）字典shopAuthType
        */
        private String authType;

        /**
         * 店铺信息
         */
        private List<ViewShopDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class ViewShopDTO {

        /**
         * 平台编号
         */
        private String dictPlatform;

        /**
         * 平台名称
         */
        private String dictPlatformName;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 是否禁用
         */
        private Boolean disabled;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


        public AddDTO (String shopId,String userId,String authType) {
            this.setShopId(shopId);
            this.setUserId(userId);
            this.setAuthType(authType);
        }
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
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 用户id
        */
        @NotBlank(message = "用户id不能为空")
        @Size(max = 19,message = "用户id最大长度不能超过19位")
        private String userId;

        /**
        * 授权类型（all全部，part部分）字典shopAuthType
        */
        @NotBlank(message = "授权类型（all全部，part部分）字典shopAuthType不能为空")
        @Size(max = 32,message = "授权类型（all全部，part部分）字典shopAuthType最大长度不能超过32位")
        private String authType;

    }

    @Data
    @NoArgsConstructor
    public static class BatchAuthDTO {
        /**
         * 店铺id集合
         */
        private List<String> shopIdList;

        /**
         * 用户id集合
         */
        @NotEmpty(message = "用户不能为空")
        private List<String> userIdList;

        /**
         * 授权类型（all全部，part部分）字典shopAuthType
         */
        @NotBlank(message = "授权类型不能为空")
        private String authType;

    }

    @Data
    @NoArgsConstructor
    public static class ViewParamDTO {

        /**
         * 用户id
         */
        @NotBlank(message = "用户id不能为空")
        private String userId;
    }

}