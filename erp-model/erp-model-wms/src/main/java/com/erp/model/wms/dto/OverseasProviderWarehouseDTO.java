package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

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
         * 货主编码
         */
        private String ownerCode;
        /**
         * 第三方仓库类型 0标准 1中转 2虚拟
         */
        private String platformWarehouseType;

        private String platformWarehouseTypeName;

        /**
        * 仓库编码
        */
        private String platformWarehouseCode;

        /**
        * 仓库名称
        */
        private String platformWarehouseName;

        /**
         * 	第三方仓库状态 0:不可用;1:可用;2:停用
         */
        private String platformWarehouseStatus;

        private String platformWarehouseStatusName;


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

    /**
     * 配送信息查询
     */
    @Data
    @NoArgsConstructor
    public static class ShippedDTO {
        /**
         * 站点
         */
        private String site;
        /**
         * 平台产品ID spu
         */
        private String platformProductId;
        /**
         * 平台SKU
         */
        private String platformSku;
        /**
         * 仓库编码列表
         */
        private List<String> warehouseCodeList;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShippedViewDTO {
        /**
         * 平台SKU
         */
        private String platformSku;
        /**
         * 三方仓库编码
         */
        private String warehouseCode;
        /**
         * 发货在途数量
         */
        private Integer deliverOnwayQty;
        /**
         * 待上架数量
         */
        private Integer pendingQty;
        /**
         * 可售数量
         */
        private Integer sellableQty;
        /**
         * 不可售数量
         */
        private Integer unsellableQty;
        /**
         * 待出库数量
         */
        private Integer reservedQty;
        /**
         * 尾程在途
         */
        private Integer onwayQty;
        /**
         * 缺货数量
         */
        private Integer lackQty;
        /**
         * 冻结数量
         */
        private Integer frozenQty;
        /**
         * 历史出库数量
         */
        private Integer shippedQty;

    }
}