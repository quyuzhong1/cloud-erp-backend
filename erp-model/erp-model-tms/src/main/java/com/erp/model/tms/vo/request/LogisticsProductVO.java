package com.erp.model.tms.vo.request;

import com.erp.model.plm.entity.ProductLogisticsEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class LogisticsProductVO extends ProductLogisticsEntity implements Serializable {

    //申报单价
    private BigDecimal price;

    //数量
    private Integer quantity;

    //单件重量(单位:g)
    private Integer weight;

    //商品链接
    private String url;

    //是否带电
    private boolean isElectric;

    //备注
    private String remark;

    //配货信息
    private String distributionInfo;

}
