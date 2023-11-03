package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FBA货件DTO 所有平台(签收明细)通用数据，转换为此类后发送mq统一消费处理
 *
 * @author Jim
 * @date 2023/11/1
 **/
@Data
@NoArgsConstructor
public class PlatformFbaShipmentReceiveDTO {
    /**
     * 亚马逊FBA货件单号
     */
    private String fbaShipmentId;
    /**
     * FNSKU
     */
    private String fnSku;
    /**
     * 卖家sku
     */
    private String mSku;
    /**
     * 申报数量
     */
    private Integer declareQty;
    /**
     * 发货数量
     */
    private Integer deliveryQty;
    /**
     * 收货数量
     */
    private Integer receiveQty;
    /**
     * 最新签收日期
     */
    private LocalDateTime receiveDate;

    public Integer calculateDiffQty(){
        return this.deliveryQty - this.receiveQty;
    }

}
