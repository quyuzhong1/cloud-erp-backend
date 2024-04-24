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
        private String logisticsSaleChannelId;


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
        private String logisticsSaleChannelId;

        /**
        * 销售平台
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=channelSalesPlatform
        */
        @NotBlank(message = "物流平台不能为空")
        @Size(max = 30,message = "物流平台最大长度不能超过30位")
        private String salesPlatform;

        /**
         * 标记发货订单类型（transportNo运单号、trackNo跟踪号）
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=orderDeliveryMarkType
         */
        @NotBlank(message = "标发订单类型不能为空")
        @Size(max = 30,message = "标发订单类型最大长度不能超过32位")
        private String orderDeliveryMarkType;
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
        private String logisticsSaleChannelId;
    }

}