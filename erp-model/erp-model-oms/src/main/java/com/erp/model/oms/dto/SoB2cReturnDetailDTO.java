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
 * b2c退货订单明细请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
*/
@Data
@NoArgsConstructor
public class SoB2cReturnDetailDTO implements Serializable {




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
        * 退货数量
        */
        private Integer returnQty;

        /**
        * 备注
        */
        private String remark;


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
        private String mainId;

        /**
        * skuId
        */
        private String skuId;

        /**
         * skuNo
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
        * 退货数量
        */
        private Integer returnQty;

        /**
        * 备注
        */
        private String remark;

        /**
         * 销售明细id
         */
        private String soDetailId;
    }


}