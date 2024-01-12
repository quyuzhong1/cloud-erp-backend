package com.erp.model.srm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 送货单明细请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
*/
@Data
@NoArgsConstructor
public class DeliveryOrderDetailDTO implements Serializable {




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
        * 送货单Id
        */
        private String mainId;

        /**
        * 来源id明细
        */
        private String sourceDetailId;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku编码
        */
        private String skuNo;

        /**
        * 送货数量
        */
        private Integer deliveryQty;

        /**
        * 赠品数量
        */
        private Integer giftQty;

        /**
        * 收货数量
        */
        private Integer receiveQty;

        /**
        * 赠品收货数量
        */
        private Integer giftReceiveQty;

        /**
        * 质检合格数
        */
        private Integer qcGoodQty;

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
        * 送货单Id
        */
        @NotBlank(message = "送货单Id不能为空")
        @Size(max = 19,message = "送货单Id最大长度不能超过19位")
        private String mainId;

        /**
        * 来源id明细
        */
        @NotBlank(message = "来源id明细不能为空")
        @Size(max = 19,message = "来源id明细最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 送货数量
        */
        @NotNull(message = "送货数量不能为空")
        private Integer deliveryQty;

        /**
        * 赠品数量
        */
        @NotNull(message = "赠品数量不能为空")
        private Integer giftQty;

        /**
        * 收货数量
        */
        @NotNull(message = "收货数量不能为空")
        private Integer receiveQty;

        /**
        * 赠品收货数量
        */
        @NotNull(message = "赠品收货数量不能为空")
        private Integer giftReceiveQty;

        /**
        * 质检合格数
        */
        @NotNull(message = "质检合格数不能为空")
        private Integer qcGoodQty;

        /**
        * 备注
        */
        private String remark;


    }


}