package com.erp.model.tms.vo.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogisticsInterceptOrderVO extends LogisticsQueryBaseVO implements Serializable {

    //拦截原因
    private String interceptReason;
}
