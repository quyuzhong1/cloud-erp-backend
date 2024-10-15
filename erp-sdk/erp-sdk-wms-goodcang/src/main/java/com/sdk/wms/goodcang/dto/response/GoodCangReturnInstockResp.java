package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangReturnInstockResp extends CleanBaseDTO implements Serializable {

    //退件单号
    @JSONField(name = "asro_code")
    private String asroCode;

    //订单号
    @JSONField(name = "order_code")
    private String orderCode;

    //退货参考号
    @JSONField(name = "reference_no")
    private String returnReferenceNo;

    //订单参考号
    @JSONField(name = "order_reference_no")
    private String orderReferenceNo;

    //退件状态
    @JSONField(name = "asro_status")
    private String asroStatus;

    //退件类型
    @JSONField(name = "cass_type")
    private Integer cassType;

    //上架完成时间
    @JSONField(name = "asro_putaway_time")
    private LocalDateTime asroPutawayTime;

    //创建时间
    @JSONField(name = "asro_add_time")
    private LocalDateTime asroAddTime;

    //明细
    @JSONField(name = "product_detail")
    private List<GoodCangReturnInstockResp.Product> productDetailList;

    @EqualsAndHashCode(callSuper = true)
    @Data
    @ToString
    public static class Product extends CleanBaseDTO {

        //商品SKU
        @JSONField(name = "product_sku")
        private String productSku;

        //良品数量
        @JSONField(name = "sellable_qty")
        private Integer sellableQty;

        //不良品数量
        @JSONField(name = "unsellable_qty")
        private Integer unsellableQty;

        //弃货数量
        @JSONField(name = "destruction_qty")
        private Integer destructionQty;

        //上架数量
        @JSONField(name = "putaway_qty")
        private Integer putawayQty;
    }
}
