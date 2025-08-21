package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  平台退货入库DTO,所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformReturnInstockDTO extends UniqueDto {

    /**
     * overseas_provider授权id
     */
    private String authId;

    /**
     * 平台退货单号
     */
    private String platformReturnOrderNo;
    /**
     * 平台订单号
     */
    private String platformOrderNo;
    /**
     * 订单参考号
     */
    private String orderReferenceNo;

    /**
     * 退货状态
     */
    private String status;

    /**
     * 退货物流单号
     */
    private String returnLogisticCode;
    /**
     * 退货类型
     */
    private String returnType;

    //上架完成时间
    private LocalDateTime putawayTime;

    //创建时间
    private LocalDateTime createTime;

    //仓库（第三方）
    private String warehouseCode;

    //备注
    private String reason;

    private String sourceId;

    //明细
    private List<PlatformReturnInstockDTO.Detail> productDetailList;


    @Data
    @ToString
    public static class Detail {

        //商品SKU(第三方)
        private String productSku;

        //应退数量
        private Integer mustQty;

        //签收数量
        private Integer receiveQty;

        //实退数量
        private Integer realQty;

        //明细唯一ID
        private String thirdId;
    }
}
