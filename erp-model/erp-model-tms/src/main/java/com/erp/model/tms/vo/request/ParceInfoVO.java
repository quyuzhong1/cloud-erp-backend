package com.erp.model.tms.vo.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 包裹信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParceInfoVO implements Serializable {

    //是否带电 1:是 0:否
    private Boolean hasBattery;

    private String currency;

    //申报总价值
    private BigDecimal totalPrice;

    //申报总数量
    private Integer totalQuantity;

    //总重量(单位:g)
    private Integer totalWeight;

    //包裹高(单位:cm)
    private Integer height;

    //包裹宽(单位:cm)
    private Integer width;

    //包裹长(单位:cm)
    private Integer length;

    //保险金额，默认RMB
    private Number insuranceValue;

    //报价金额，默认RMB
    private Number insureValue;

    //物品类型（0、礼物；1、文件;2、商业样本;3、回货品;4、其他）,去发货填写，不填默认4
    private Integer itemType;
}
