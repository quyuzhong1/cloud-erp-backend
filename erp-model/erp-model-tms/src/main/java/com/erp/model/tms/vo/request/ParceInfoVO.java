package com.erp.model.tms.vo.request;

import com.baomidou.mybatisplus.annotation.TableField;
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
    private Integer hasBattery;

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

    //IOSS 税号
    private String ioss;
}
