package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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
        * 渠道id的json
        */
        private String channelIdJson;

        /**
         * 渠道id的json
         */
        private String channelIdJsonName;

        /**
         * 平台集合
         */
        private List<String> platformList;

        /**
         * 渠道(店铺)id的json
         */
        private List<String> channelIdList;

        /**
        * 库存分配类型
        */
        private String inventoryAllocateType;

        /**
        * 主表id
        */
        private String mainId;


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
        * 虚拟仓id
        */
        @Size(max = 19,message = "虚拟仓id最大长度不能超过19位")
        private String virtualWarehouseId;

        /**
        * 仓库类型，local本地，overseas海外
        */
        @NotBlank(message = "仓库类型，local本地，overseas海外不能为空")
        @Size(max = 32,message = "仓库类型，local本地，overseas海外最大长度不能超过32位")
        private String warehouseType;

        /**
        * 关联店铺类型，platform按平台，shop按店铺
        */
        @NotBlank(message = "关联店铺类型，platform按平台，shop按店铺不能为空")
        @Size(max = 32,message = "关联店铺类型，platform按平台，shop按店铺最大长度不能超过32位")
        private String channelType;

        /**
        * 渠道(店铺)id的json
        */
        @NotEmpty(message = "渠道id集合")
        private List<String> channelIdList;

        /**
        * 库存分配类型
        */
        @NotBlank(message = "库存分配类型不能为空")
        @Size(max = 32,message = "库存分配类型最大长度不能超过32位")
        private String inventoryAllocateType;

    }


}