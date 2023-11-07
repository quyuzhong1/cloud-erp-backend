package com.erp.model.tms.vo.request;

import com.erp.model.plm.entity.ProductLogisticsEntity;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;


@Data
@Builder
public class LogisticsProductVO extends ProductLogisticsEntity implements Serializable {

    //申报单价
    private BigDecimal price;

    //数量
    private Integer quantity;

    //单件重量(单位:g)
    private Integer weight;

    //商品链接
    private String url;
}
