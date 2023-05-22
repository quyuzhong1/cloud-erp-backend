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
         * 禁用启用
         * true禁用；false启用
         */
        private Boolean disabled;

        /**
         * 是否可以选择（禁用停用的置灰）
         */
        private Boolean canCheck;

    }


}