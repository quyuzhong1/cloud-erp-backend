package com.erp.model.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 物流渠道映射表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@NoArgsConstructor
public class LogisticsMappingDTO implements Serializable {




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
        * 物流平台
        */
        private String salesPlatform;

        /**
         *  物流销售渠道id
         */
        private String platformLogisticsChannelId;
        /**
         * 物流销售渠道名称
         */
        private String platformLogisticsChannelName;

        /**
         * 标记发货订单类型（transportNo运单号、trackNo跟踪号）
         */
        private String orderDeliveryMarkType;

        /**
         * 承运商代号(部分速卖通物流渠道必填)
         */
        private String carrierCode;

        /**
         * 承运商代号名称
         */
        private String carrierName;


        /**
         * 仓库id
         */
        private String warehouseId;

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
        * 物流渠道id
        */
        @NotBlank(message = "销售的渠道不能为空")
        private String platformLogisticsChannelId;

        /**
        * 销售平台
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=channelSalesPlatform
        */
        private String salesPlatform;

        /**
         * 标记发货订单类型（transportNo运单号、trackNo跟踪号）
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=orderDeliveryMarkType
         */
        private String orderDeliveryMarkType;


        /**
         * 承运商代号(部分速卖通物流渠道必填)
         * <a href="http://172.16.100.11:3002/project/128/interface/api/cat_1635">来源：承运商列表</a>
         */
        private String carrierCode;

        /**
         * 仓库id
         */
        private String warehouseId;

        private String type;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchParamDTO {

        /**
         * 销售平台
         */
        @NotBlank(message = "销售平台不能为空")
        private String salesPlatform;

        /**
         * 物流渠道id
         */
        @NotBlank(message = "物流渠道id不能为空")
        private String logisticsChannelId;

        /**
         * 销售渠道id
         */
        @NotBlank(message = "销售渠道id不能为空")
        private String logisticsSaleChannelId;
    }

}