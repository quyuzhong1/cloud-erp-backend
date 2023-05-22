package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
         * 状态名称
         */
        private String statusName;

        /**
         * 是否可以选择（仓位状态为停用的和仓位禁用的置灰）
         */
        private Boolean canCheck;

    }


}