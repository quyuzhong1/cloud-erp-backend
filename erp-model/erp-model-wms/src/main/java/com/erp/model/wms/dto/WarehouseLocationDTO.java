package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * 仓位请求响应实体
 * @CreateTime: 2023-05-22  09:33
 * @Author: zhangchunlin
 */
@Data
public class WarehouseLocationDTO implements Serializable {

    /**
     * 仓位详情
     */
    @Data
    @NoArgsConstructor
    public static class LocationDetailDTO {

        /**
         * 类型
         */
        private String type;

        /**
         * 类型编码
         */
        private String typeName;

        /**
         * 编码
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        /**
         * 状态
         */
        private String status;

        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 分区id
         */
        private String areaId;

        /**
         * 分区类型
         */
        private String areaType;

        /**
         * 分区名称
         */
        private String areaName;

        /**
         * 禁用状态，true表示禁用
         */
        private Boolean disabled;

        /**
         * 备注
         */
        private String remark;

    }
    @Data
    @NoArgsConstructor
    public static class WarehouseLocationListDTO {
        private String warehouseId;
        private List<LocationListDTO> locationList;
    }

    /**
     * 仓位列表
     */
    @Data
    @NoArgsConstructor
    public static class LocationListDTO {

        /**
         * id
         */
        private String id;

        /**
         * 编码
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        /**
         * 状态
         */
        private String status;

        /**
         * 是否禁用（true是，false否）
         */
        private Boolean disabled;

        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 是否可以选择（仓位状态为停用的和仓位禁用的置灰）
         */
        private Boolean canCheck;

    }


    /**
     * 仓位下拉
     */
    @Data
    @NoArgsConstructor
    public static class LocationSelectDTO {

        /**
         * 编码
         */
        private String code;

        /**
         * 名称
         */
        private String name;

    }

    /**
     * 仓位查询实体
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseLocationSearchParamDTO extends SortDTO {

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓位编码
         */
        private String code;

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingViewDTO {

        /**
         * 仓位id
         */
        private String warehouseLocationId;

        /**
         * 仓位编码
         */
        private String warehouseLocation;

        /**
         * 仓位编码
         */
        private String warehouseLocationName;

        /**
         * 区域编码
         */
        private String warehouseArea;
        /**
         * 区域名称
         */
        private String warehouseAreaName;

        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 组织id
         */
        private String orgId;
        /**
         * 组织名称
         */
        private String orgName;

        /**
         * 仓库类型
         */
        private String warehouseType;
        /**
         * 仓库id
         */
        private String typeId;
    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {

        /**
         * 仓库类型
         */
        private List<String> typeIds;

        /**
         * 仓库组织
         */
        private List<String> orgIds;
        /**
         * 仓库id
         */
        private List<String> warehouseIds;

        /**
         * 仓库区域 id
         */
        private List<String> warehouseAreas;
        /**
         * 仓位编码
         */
        private String warehouseLocation;
    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         * 仓库Id
         */
        @NotBlank(message = "仓库ID不能为空")
        private String warehouseId;

        /**
         * 关键词
         */
        private String searchKeyword;

    }
}