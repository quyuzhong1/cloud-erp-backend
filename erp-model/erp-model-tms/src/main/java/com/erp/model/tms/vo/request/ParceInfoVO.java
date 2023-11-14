package com.erp.model.tms.vo.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.core.anno.StateEnumValue;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 包裹信息
 */
@Data
@Builder
public class ParceInfoVO {

    //是否带电 1:是 0:否
    private Boolean hasBattery;

    //币种代码传 USD,EUR,GBP,CNY,AUD,CAD;
    @StateEnumValue(strValues = {"USD", "EUR","GBP","CNY","AUD","CAD"}, message = "币种代码有误")
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
