package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
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
         * 虚拟仓id
         */
        private String  virtualWarehouseId;
        /**
         * 虚拟仓编号
         */
        private String  virtualWarehouseCode;
        /**
         * 虚拟仓名称
         */
        private String  virtualWarehouseName;

        //海外 渠道列表
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> overseasChannelList;
        //国内 渠道列表
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> internalChannelList;
        //其他 渠道列表
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> otherChannelList;

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
    public static class BatchUpdateDTO {
        /**
         * 虚拟仓id
         */
        @NotBlank(message = "虚拟仓库id不能为空")
        private String virtualWarehouseId;
        //海外 渠道列表
        @Valid
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> overseasChannelList;
        //国内 渠道列表
        @Valid
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> internalChannelList;
        //其他 渠道列表
        @Valid
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> otherChannelList;

    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChannelAddDTO{
        /**
         * 平台的dict值
         */
        @NotBlank(message = "销售平台不能为空")
        private String dictPlatform;
        /**
         * 平台明细
         */
        private List<VirtualWarehouseChannelDTO.DetailDTO> detailDTOList;
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
         * 分区id
         */
        private String partitionId;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailDTO {

        /**
         * 店铺集合 根据id排序后 加密【后端使用】
         */
        private String shopMd5;
        /**
         * 店铺id集合
         */
        private List<String> shopIdList;
        /**
         * 店铺名称集合
         */
        private List<String> shopNameList;
        /**
         * 分区列表
         */
        private List<String> partitonIdList;
        /**
         *
         */
        private List<String> partitonNameList;
    }
}