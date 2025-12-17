package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-08-29
*/
@Data
@NoArgsConstructor
public class DmpPlatformSoDeliveryDTO implements Serializable {




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
        * 卖家订单编号
        */
        private String code;

        /**
        * 亚马逊订单编号

        */
        private String platformCode;

        /**
        * 发货状态
        */
        private String deliveryStatus;

        /**
        * 发货时间（东八区）
        */
        private LocalDateTime deliveryTime;

        /**
        * 物流跟踪号
        */
        private String trackNo;

        /**
        * 订单类型（soMultiChannel多渠道）
        */
        private String orderType;

        /**
        * 货件id
        */
        private String shipmentId;


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
        * 亚马逊订单编号

        */
        @NotBlank(message = "亚马逊订单编号不能为空")
        @Size(max = 100,message = "亚马逊订单编号最大长度不能超过100位")
        private String platformCode;

        /**
        * 发货状态
        */
        @NotBlank(message = "发货状态不能为空")
        @Size(max = 50,message = "发货状态最大长度不能超过50位")
        private String deliveryStatus;

        /**
        * 发货时间（东八区）
        */
        private LocalDateTime deliveryTime;

        /**
        * 物流跟踪号
        */
        @NotBlank(message = "物流跟踪号不能为空")
        @Size(max = 100,message = "物流跟踪号最大长度不能超过100位")
        private String trackNo;

        /**
        * 订单类型（soMultiChannel多渠道）
        */
        @NotBlank(message = "订单类型（soMultiChannel多渠道）不能为空")
        @Size(max = 50,message = "订单类型（soMultiChannel多渠道）最大长度不能超过50位")
        private String orderType;

        /**
        * 货件id
        */
        @NotBlank(message = "货件id不能为空")
        @Size(max = 50,message = "货件id最大长度不能超过50位")
        private String shipmentId;


    }


}