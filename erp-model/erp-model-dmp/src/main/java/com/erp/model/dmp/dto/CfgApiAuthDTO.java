package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: API授权信息DTO
 * @date 2023/1/11 16:59
 */
@Data
@NoArgsConstructor
public class CfgApiAuthDTO {

    @Data
    @NoArgsConstructor
    public static class ParamDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 平台id
         */
        private String apiPlatformId;

        /**
         * 组别,默认default
         */
        private String apiGroup;

        /**
         * 键 (英文描述)
         */
        @NotBlank(message = "英文描述不能为空")
        private String key;

        /**
         * 授权信息（存json字符串）
         */
        @NotBlank(message = "授权信息不能为空")
        private String value;
    }

    @Data
    @NoArgsConstructor
    public static class FeignDTO {

        /**
         * 键
         */
        @NotBlank(message = "键值不能为空")
        private String key;

        /**
         * 平台id
         */
        private String apiPlatformId;

        /**
         * 组别,默认default
         */
        private String apiGroup;


        public FeignDTO (String key) {
            this.key = key;
        }

    }

    @Data
    @NoArgsConstructor
    public static class KingDeeCreateOrgDTO {

        /**
         * 第一级创建组织id
         */
        private Integer  firstOrgId;

        /**
         * 第二级创建组织id
         */
        private Integer secondOrgId;

        /**
         * 组织id集合
         */
        private List<Integer> orgIdList;

    }

    @Data
    @NoArgsConstructor
    public static class WarehouseLocationValidateDTO {

        /**
         * 仓库id，逗号拼接
         */
        private String warehouseIds;
    }
}
