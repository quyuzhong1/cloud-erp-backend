package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 仓库映射第三方平台表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-30
*/
@Data
@NoArgsConstructor
public class WarehouseMappingDTO implements Serializable {




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
        * 仓库id
        */
        private String warehouseId;

        /**
        * 第三方平台仓库名称
        */
        private String name;

        /**
        * 所属平台编码
        */
        private String dictPlatform;

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
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 第三方平台仓库名称
        */
        @NotBlank(message = "第三方平台仓库名称不能为空")
        @Size(max = 255,message = "第三方平台仓库名称最大长度不能超过255位")
        private String name;

        /**
        * 所属平台编码
        */
        @NotBlank(message = "所属平台编码不能为空")
        @Size(max = 255,message = "所属平台编码最大长度不能超过255位")
        private String dictPlatform;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


    /**
     * 映射信息
     */
    @Data
    @NoArgsConstructor
    public static class MappingViewDTO {
        /**
         * ERP仓库id
         */
        private String warehouseId;
        /**
         * ERP仓库名称
         */
        private String warehouseName;
        /**
         * 第三方平台仓库名称
         */
        private String thirdWarehouseName;
        /**
         * 所属平台编码
         */
        private String dictPlatform;
    }
}