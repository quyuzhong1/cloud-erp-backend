package com.erp.model.tms.vo.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class LogisticsGetLabelVO extends LogisticsQueryBaseVO implements Serializable {
    //打印拣货单
    private Integer printRemark;
}
