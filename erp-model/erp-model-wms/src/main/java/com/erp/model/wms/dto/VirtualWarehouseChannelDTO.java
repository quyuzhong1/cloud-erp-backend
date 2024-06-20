package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 虚拟仓渠道请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
*/
@Data
@NoArgsConstructor
public class VirtualWarehouseChannelDTO implements Serializable {




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
        * 是否失效 true 失效 false 未失效
        */
        private Boolean disabled;

        /**
        * 虚拟仓id
        */
        private String virtualWarehouseId;

        /**
        * 虚拟仓编码
        */
        private String virtualWarehouseCode;

        /**
        * 虚拟仓名称
        */
        private String virtualWarehouseName;

        /**
        * 平台的dict值
        */
        private String dictPlatform;

        /**
        * 关联类型：  platform 按平台 shop 按店铺
        */
        private String type;

        /**
        * 关联id（例如店铺）
        */
        private String relationId;

        /**
        * 关联名称
        */
        private String relationName;

        /**
        * 平台类型
        */
        private String dictPlatformType;


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
        * 是否失效 true 失效 false 未失效
        */
        private Boolean disabled;

        /**
        * 虚拟仓id
        */
        @Size(max = 19,message = "虚拟仓id最大长度不能超过19位")
        private String virtualWarehouseId;

        /**
        * 虚拟仓编码
        */
        @Size(max = 30,message = "虚拟仓编码最大长度不能超过30位")
        private String virtualWarehouseCode;

        /**
        * 虚拟仓名称
        */
        @Size(max = 200,message = "虚拟仓名称最大长度不能超过200位")
        private String virtualWarehouseName;

        /**
        * 平台的dict值
        */
        @NotBlank(message = "平台的dict值不能为空")
        @Size(max = 30,message = "平台的dict值最大长度不能超过30位")
        private String dictPlatform;

        /**
        * 关联类型：  platform 按平台 shop 按店铺
        */
        @NotBlank(message = "关联类型：  platform 按平台 shop 按店铺不能为空")
        @Size(max = 20,message = "关联类型：  platform 按平台 shop 按店铺最大长度不能超过20位")
        private String type;

        /**
        * 关联id（例如店铺）
        */
        @Size(max = 19,message = "关联id（例如店铺）最大长度不能超过19位")
        private String relationId;

        /**
        * 关联名称
        */
        @Size(max = 100,message = "关联名称最大长度不能超过100位")
        private String relationName;

        /**
        * 平台类型
        */
        @Size(max = 255,message = "平台类型最大长度不能超过255位")
        private String dictPlatformType;


    }

    @Data
    @NoArgsConstructor
    public static class BatchAddDTO{
        private List<VirtualWarehouseChannelDTO.ChannelAddDTO> channelList;
        private String virtualWarehouseId;
    }
    @Data
    @NoArgsConstructor
    public static class ChannelAddDTO{
        /**
         * 平台类型
         */
        private String dictPlatformType;
        /**
         * 平台的dict值
         */
        @NotBlank(message = "渠道不能为空")
        private String dictPlatform;

        /**
         * 关联类型：  platform 按平台 shop 按店铺
         */
        @NotBlank(message = "渠道类型不能为空")
        private String type;

        /**
         * 关联id（例如店铺）
         */
        private List<String> relationList;
        /**
         * 是否失效 true 失效 false 未失效
         */
        private Boolean disabled=false;
    }


    @Data
    @NoArgsConstructor
    public static class PlatformDTO{

        /**
         * 关联id（如店铺id）,无关联id时传空字符
         */
        @NotNull(message = "关联id不能为null")
        private String relationId;

        /**
         * 平台
         */
        @NotBlank(message = "平台不能为空")
        private String dictPlatform;

        /**
         * 实体仓Id集合
         */
        @NotEmpty(message = "实体仓不能为空")
        private List<String> warehouseIdList;
    }

    @Data
    @NoArgsConstructor
    public static class ListPlatformDTO{

        /**
         * 平台
         */
        @NotEmpty(message = "平台不能为空")
        private List<String> dictPlatformList;

        /**
         * 实体仓Id集合
         */
        @NotEmpty(message = "实体仓不能为空")
        private List<String> warehouseIdList;
    }
}