package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 三方仓发货单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
*/
@Data
@NoArgsConstructor
public class ThirdWarehouseDeliveryDTO implements Serializable {




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
        * 三方仓出库单号
        */
        private String code;

        /**
        * 销售单号
        */
        private String soCode;

        /**
        * 销售id
        */
        private String soId;

        /**
        * 平台
        */
        private String dictPlatform;

        /**
        * 平台订单号
        */
        private String platformCode;

        /**
        * 三方仓平台
        */
        private String thirdWarehousePlatform;


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
        * 销售单号
        */
        @NotBlank(message = "销售单号不能为空")
        @Size(max = 50,message = "销售单号最大长度不能超过50位")
        private String soCode;

        /**
        * 销售id
        */
        @NotBlank(message = "销售id不能为空")
        @Size(max = 50,message = "销售id最大长度不能超过50位")
        private String soId;

        /**
        * 平台
        */
        @NotBlank(message = "平台不能为空")
        @Size(max = 50,message = "平台最大长度不能超过50位")
        private String dictPlatform;

        /**
        * 平台订单号
        */
        @NotBlank(message = "平台订单号不能为空")
        @Size(max = 100,message = "平台订单号最大长度不能超过100位")
        private String platformCode;

        /**
        * 三方仓平台
        */
        @NotBlank(message = "三方仓平台不能为空")
        @Size(max = 255,message = "三方仓平台最大长度不能超过255位")
        private String thirdWarehousePlatform;


    }


}