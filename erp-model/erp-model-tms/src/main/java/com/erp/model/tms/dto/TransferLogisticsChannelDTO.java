package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 中转报关服务商渠道表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class TransferLogisticsChannelDTO implements Serializable {




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
        * 渠道名称
        */
        private String name;

        /**
        * 渠道代码
        */
        private String code;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 中转报关服务商表id，表名称：transfer_logistics_supplier
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
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 渠道名称
        */
        @NotBlank(message = "渠道名称不能为空")
        @Size(max = 100,message = "渠道名称最大长度不能超过100位")
        private String name;

        /**
        * 渠道代码
        */
        @NotBlank(message = "渠道代码不能为空")
        @Size(max = 50,message = "渠道代码最大长度不能超过50位")
        private String code;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
         * 物流平台编号
         */
        @NotBlank(message = "物流平台编号不能为空")
        @Size(max = 50,message = "物流平台编号最大长度不能超过50位")
        private String logisticsPlatform;
    }

    @Data
    @NoArgsConstructor
    public static class ListSelectDTO {

        /**
         * 中转报关服务商id
         */
        private String transferLogisticsSupplierId;

        /**
         * 中转报关服务商名称
         */
        private String transferLogisticSupplierName;

        /**
         * 渠道名称
         */
        private String name;
        /**
         * 渠道编码
         */
        private String code;

        /**
         * 渠道id
         */
        private String id;

        /**
         * 是否禁用
         */
        private Boolean disabled;
    }


    @Data
    @NoArgsConstructor
    public static class EditDeliveryCountryDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 发货国家编码
         */
        private String countryCode;

        /**
         * 发货国家名称
         */
        private String countryName;
    }
}