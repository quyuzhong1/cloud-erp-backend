package com.erp.model.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 用户-店铺权限请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-02-27
*/
@Data
@NoArgsConstructor
public class AuthUserShopDTO implements Serializable {




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
        * 用户id
        */
        private String userId;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 数据权限(0-全部，1-部分)
        */
        private Integer dataScope;


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
        * 用户id
        */
        @NotBlank(message = "用户id不能为空")
        @Size(max = 64,message = "用户id最大长度不能超过64位")
        private String userId;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 数据权限(0-全部，1-部分)
        */
        private Integer dataScope;


    }


    @Data
    @NoArgsConstructor
    public static class AddUserShopAuthDTO {
        private String userId;
        private List<String> shopIdList;
    }

    /**
     * 平台权限店铺查询参数
     */
    @Data
    @NoArgsConstructor
    public static class ShopAuthParamDTO {
        /**
         * 平台
         */
        private String platform;
    }

    /**
     * 平台权限店铺查询结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopAuthListDTO {
        /**
         * 平台
         */
        private String platform;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;

    }

}