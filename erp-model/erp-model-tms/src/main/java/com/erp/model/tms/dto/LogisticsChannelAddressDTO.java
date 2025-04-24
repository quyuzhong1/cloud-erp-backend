package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * 渠道地址表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@NoArgsConstructor
public class LogisticsChannelAddressDTO implements Serializable {




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
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 地址id
        */
        private String addressId;

        /**
         * 地址类型
         * deliver 发货
         * refund 退货
         * collect 揽收
         */
        private String logisticsAddressType;

        private String logisticsAddressName;


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
        * 店铺id
        */
        private String shopId;



        /**
        * 地址id
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25783
        */
        @NotBlank(message = "地址id不能为空")
        private String addressId;


    }


}