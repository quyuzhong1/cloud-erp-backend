package com.erp.model.tms.vo.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class LogisticsCancelOrderVO extends LogisticsQueryBaseVO implements Serializable {
    //打印拣货单
    private Integer printRemark;

    //取消原因
    private String reason;
}
