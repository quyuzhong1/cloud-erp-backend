package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 店铺与第三方平台对照表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-02-19
*/
@Data
@NoArgsConstructor
public class ShopInfoMappingDTO implements Serializable {


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
         * 店铺表id
         */
        private String shopId;

        /**
         * 同步第三方平台类型
         */
        private String thirdPlatformType;

        private String thirdPlatformShopId;

        /**
         * 备注
         */
        private String remark;


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
         * 店铺表id
         */
        @NotBlank(message = "店铺表id不能为空")
        @Size(max = 19, message = "店铺表id最大长度不能超过19位")
        private String shopId;

        /**
         * 同步第三方平台类型
         */
        @NotBlank(message = "同步第三方平台类型不能为空")
        @Size(max = 16, message = "同步第三方平台类型最大长度不能超过16位")
        private String thirdPlatformType;

        /**
         * 同步第三方平台店铺ID
         */
        @NotBlank(message = "同步第三方平台店铺ID不能为空")
        @Size(max = 16, message = "同步第三方平台店铺ID最大长度不能超过16位")
        private String thirdPlatformId;
    }
}
