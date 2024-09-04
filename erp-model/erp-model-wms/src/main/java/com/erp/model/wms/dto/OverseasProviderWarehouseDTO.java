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
 * @author Luo_WG
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

        /**
         * 海外仓服务商code
         */
        private String  providerCode;


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
    public static class UpdateDTO {
        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 系统仓库id
         */
        @NotBlank(message = "系统仓库id不能为空")
        private String warehouseId;

        /**
         * 是否禁用 true 禁用 false 启用
         */
        private Boolean disabled;
        /**
         * 仓库编码
         */
        private String platformWarehouseCode;

        /**
         * 仓库名称
         */
        private String platformWarehouseName;
        /**
         * 系统仓库名称
         */
        private String warehouseName;

        /**
         * 系统仓库编码
         */
        private String warehouseCode;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

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

    @Data
    @NoArgsConstructor
    public static class PageSelectDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 名称
         */
        private String name;
        /**
         * 编号
         */
        private String code;
    }

    /**
     * 远程搜索
     */
    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 系统类型
         */
        @NotBlank(message = "平台编码不能为空")
        private String code;
        /**
         * 仓库简称
         */
        private String shortName;

        /**
         * 关键词
         */
        private String searchKeyword;
    }

}