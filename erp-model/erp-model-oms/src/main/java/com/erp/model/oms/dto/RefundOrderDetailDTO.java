package com.erp.model.oms.dto;

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
 * 退款订单明细请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-09-27
*/
@Data
@NoArgsConstructor
public class RefundOrderDetailDTO implements Serializable {




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
        * skuId
        */
        private String skuId;

        /**
        * 产品sku编号
        */
        private String skuNo;

        /**
        * 平台sku
        */
        private String platformSkuNo;

        /**
        * 销售数量
        */
        private Integer saleQty;

        /**
        * 退款数量
        */
        private Integer refundQty;


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
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 100,message = "平台sku最大长度不能超过100位")
        private String platformSkuNo;

        /**
        * 销售数量
        */
        @NotNull(message = "销售数量不能为空")
        private Integer saleQty;

        /**
        * 退款数量
        */
        @NotNull(message = "退款数量不能为空")
        private Integer refundQty;


    }


}