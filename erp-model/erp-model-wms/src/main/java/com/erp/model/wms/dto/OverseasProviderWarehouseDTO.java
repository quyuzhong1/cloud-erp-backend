package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 海外物流商仓库请求响应实体
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class OverseasProviderWarehouseDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 仓库编码
        */
        private String platformWarehouseCode;

        /**
        * 仓库名称
        */
        private String platformWarehouseName;

        /**
        * 国家二字码
        */
        private String country;

        /**
        * 国家中文名
        */
        private String countryName;

        /**
        * 系统仓库id
        */
        private String warehouseId;

        /**
        * 系统仓库名称
        */
        private String warehouseName;

        /**
        * 系统仓库编码
        */
        private String warehouseCode;

        /**
        * 是否禁用 true 禁用 false 启用
        */
        private Boolean disabled;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 仓库编码
        */
        @NotBlank(message = "仓库编码不能为空")
        @Size(max = 255,message = "仓库编码最大长度不能超过255位")
        private String platformWarehouseCode;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 255,message = "仓库名称最大长度不能超过255位")
        private String platformWarehouseName;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 19,message = "国家二字码最大长度不能超过19位")
        private String country;

        /**
        * 国家中文名
        */
        @NotBlank(message = "国家中文名不能为空")
        @Size(max = 255,message = "国家中文名最大长度不能超过255位")
        private String countryName;

        /**
        * 系统仓库id
        */
        @NotBlank(message = "系统仓库id不能为空")
        @Size(max = 19,message = "系统仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 系统仓库名称
        */
        @NotBlank(message = "系统仓库名称不能为空")
        @Size(max = 255,message = "系统仓库名称最大长度不能超过255位")
        private String warehouseName;

        /**
        * 系统仓库编码
        */
        @NotBlank(message = "系统仓库编码不能为空")
        @Size(max = 255,message = "系统仓库编码最大长度不能超过255位")
        private String warehouseCode;

        /**
        * 是否禁用 true 禁用 false 启用
        */
        @NotNull(message = "是否禁用 true 禁用 false 启用不能为空")
        private Boolean disabled;


    }


}