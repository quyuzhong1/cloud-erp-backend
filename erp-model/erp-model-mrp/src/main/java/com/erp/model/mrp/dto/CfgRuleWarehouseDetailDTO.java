package com.erp.model.mrp.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 仓库（规则设置）明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-24
*/
@Data
@NoArgsConstructor
public class CfgRuleWarehouseDetailDTO implements Serializable {




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
        * 实体仓id
        */
        private String warehouseId;

        /**
         * 实体仓名称
         */
        private String warehouseName;

        /**
        * 虚拟仓id
        */
        private String virtualWarehouseId;

        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;

        /**
         * 仓库类型，local本地，overseas海外
         */
        private String warehouseType;

        /**
        * 关联渠道类型，platform按平台，shop按店铺
        */
        private String channelType;

        /**
         * 关联渠道类型名称
         */
        private String channelTypeName;

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 平台名称
         */
        private String dictPlatformName;

        /**
         * 渠道(店铺)id的json
         */
        private List<String> channelIdList;

        /**
         * 渠道id的名称
         */
        private String channelIdJsonName;

        /**
         * 分区id
         */
        private List<String> partitionIdList;

        /**
         * 分区的名称
         */
        private String partitionName;

        /**
        * 主表id
        */
        private String mainId;


    }

    @Getter
    @Setter
    public static class WarehouseViewDTO {

        /**
         * 实体仓id
         */
        private String warehouseId;

        /**
         * 实体仓名称
         */
        private String warehouseName;

        /**
         * 仓库平台
         */
        private List<WarehousePlatformDTO> warehousePlatform;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class WarehouseGroupDTO {
        /**
         * 实体仓id
         */
        private String warehouseId;

        /**
         * 实体仓名称
         */
        private String warehouseName;
    }

    @Getter
    @Setter
    public static class WarehousePlatformDTO {
        /**
         * 平台
         */
        private List<String> platformList;

        /**
         * 关联渠道类型，platform按平台，shop按店铺
         */
        private String channelType;

        /**
         * 仓库平台
         */
        List<String> shopIdList;
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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 实体仓id
        */
        @NotBlank(message = "实体仓id不能为空")
        @Size(max = 19,message = "实体仓id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 关联店铺类型，platform按平台，shop按店铺
        */
        @NotBlank(message = "关联店铺类型，platform按平台，shop按店铺不能为空")
        @Size(max = 32,message = "关联店铺类型，platform按平台，shop按店铺最大长度不能超过32位")
        private String channelType;

        /**
        * 渠道(店铺)id的json
        */
        private List<String> channelIdList;
        /**
         * 分区ids
         */
        private List<String> partitionIdList;

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 虚拟仓id
         */
        @Size(max = 19,message = "虚拟实体仓id最大长度不能超过19位")
        private String virtualWarehouseId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverseasWarehouseDTO {
        /**
         * 仓库Id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
    }

    @Getter
    @Setter
    public static class WarehouseUpdateDTO {

        /**
         * 实体仓id
         */
        private String warehouseId;

        /**
         * 仓库平台
         */
        private List<WarehousePlatformDTO> warehousePlatform;
    }
}