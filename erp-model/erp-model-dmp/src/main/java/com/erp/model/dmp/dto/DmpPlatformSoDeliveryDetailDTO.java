package com.erp.model.dmp.dto;

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
public class DmpPlatformSoDeliveryDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * sellerSku
        */
        private String msku;

        /**
        * fulfillmentNetworkSku
        */
        private String fnSku;

        /**
        * 卖家订单明细ID
        */
        private String sourceDetailId;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 发货单编码
        */
        private String code;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 64,message = "主表id最大长度不能超过64位")
        private String mainId;

        /**
        * sellerSku
        */
        @NotBlank(message = "sellerSku不能为空")
        @Size(max = 100,message = "sellerSku最大长度不能超过100位")
        private String msku;

        /**
        * fulfillmentNetworkSku
        */
        @NotBlank(message = "fulfillmentNetworkSku不能为空")
        @Size(max = 100,message = "fulfillmentNetworkSku最大长度不能超过100位")
        private String fnSku;

        /**
        * 卖家订单明细ID
        */
        @NotBlank(message = "卖家订单明细ID不能为空")
        @Size(max = 100,message = "卖家订单明细ID最大长度不能超过100位")
        private String sourceDetailId;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;


    }


}