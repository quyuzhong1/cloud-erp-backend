package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 速卖通发货单详情请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-05-06
*/
@Data
@NoArgsConstructor
public class AliexpressDeliveryDetailDTO implements Serializable {




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
        * 平台sku
        */
        private String platformSku;

        /**
        * 平台发货数量
        */
        private Integer orderLineQty;

        /**
        * ERP的sku
        */
        private String skuNo;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 唯一ID
         * 速卖通=中台明细ID
         */
        private String uniqueId;
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
        private String mainId;

        /**
        * 平台sku
        */
        private String platformSku;

        /**
        * 平台发货数量
        */
        private Integer orderLineQty;

        /**
         * erp sku编号
         */
        private String skuNo;


        /**
         * erp sku id
         */
        private String skuId;

        /**
         * 平台发货状态
         * AliexpressDeliveryOrderStatusEnum
         */
        private String platformDeliveryStatus;

        /**
         * 明细单价
         */
        private BigDecimal price;

        /**
         * 币别(速卖通发货单明细来源单价币种)
         */
        private String currency;
        /**
         * 实际支付金额
         */
        private BigDecimal payAmount;

        /**
         * 实际支付币别
         */
        private String payCurrency;

        /**
         * 折扣金额
         */
        private BigDecimal discountAmount;

        /**
         * 折扣币别
         */
        private String discountCurrency;

        /**
         * 货品id
         */
        private String scItemId;

        /**
         * 平台skuId
         */
        private String platformSkuId;

        /**
         * 平台产品ID
         */
        private String platformSpuNo;

        /**
         * 平台订单明细状态
         */
        private String orderDetailPlatformStatus;
    }


}