package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 销售平台物流渠道表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
*/
@Data
@NoArgsConstructor
public class LogisticsSaleChannelDTO implements Serializable {




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
        * 渠道id(物流平台原始id)
        */
        private String platformChannelId;

        /**
        * 渠道名称(默认中文)
        */
        private String cnName;

        /**
        * 渠道名称(英文)
        */
        private String enName;

        /**
        * 渠道编码
        */
        private String code;

        /**
        * 时效
        */
        private LocalDateTime expireTime;

        /**
        * 渠道状态0正常1.暂停2.已关闭（默认0）
        */
        private Integer channelStatus;

        /**
        * 渠道供应商名称
        */
        private String supplierName;

        /**
        * 渠道供应商编码
        */
        private String supplierCode;

        /**
        * 发货方式
        */
        private String shipmentMethod;

        /**
        * 物流平台类型
        */
        private String logisticsPlatform;

        /**
        * 获取接口的原始数据
        */
        private String sourceData;

        /**
        * 是否可跟踪轨迹0是 1否（默认0）
        */
        private Boolean isTrack;

        /**
        * 快递时效
        */
        private String aging;


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

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 渠道id(物流平台原始id)
         */
        private String platformChannelId;

        /**
         * 渠道名称(默认中文)
         */
        private String cnName;

        /**
         * 渠道编码
         */
        private String code;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 渠道id(物流平台原始id)
        */
        @NotBlank(message = "渠道id(物流平台原始id)不能为空")
        @Size(max = 32,message = "渠道id(物流平台原始id)最大长度不能超过32位")
        private String platformChannelId;

        /**
        * 渠道名称(默认中文)
        */
        @NotBlank(message = "渠道名称(默认中文)不能为空")
        @Size(max = 50,message = "渠道名称(默认中文)最大长度不能超过50位")
        private String cnName;

        /**
        * 渠道名称(英文)
        */
        private String enName;

        /**
        * 时效
        */
        private LocalDateTime expireTime;

        /**
        * 渠道状态0正常1.暂停2.已关闭（默认0）
        */
        @NotNull(message = "渠道状态0正常1.暂停2.已关闭（默认0）不能为空")
        private Integer channelStatus;

        /**
        * 渠道供应商名称
        */
        private String supplierName;

        /**
        * 渠道供应商编码
        */
        private String supplierCode;

        /**
        * 发货方式
        */
        private String shipmentMethod;

        /**
        * 物流平台类型
        */
        @NotBlank(message = "物流平台类型不能为空")
        @Size(max = 50,message = "物流平台类型最大长度不能超过50位")
        private String logisticsPlatform;

        /**
        * 获取接口的原始数据
        */
        private String sourceData;

        /**
        * 是否可跟踪轨迹0是 1否（默认0）
        */
        private Boolean isTrack;

        /**
        * 快递时效
        */
        private String aging;


    }


    @Data
    @NoArgsConstructor
    public static class QueryDTO {

        @NotBlank(message = "平台类型不能为空")
        private String platformType;
        /**
         * oms tms
         */
        @NotBlank(message = "服务系统不能为空")
        private String servicePlatform;
    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 销售平台
         */
        private String salesPlatform = "AliExpress";
        /**
         * 模糊搜索
         */
        private String searchKeyword;
    }
}