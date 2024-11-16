package com.erp.model.tms.vo.request;

import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogisticsGetLabelVO extends LogisticsQueryBaseVO implements Serializable {

    //面单类型 1 100X100
    private String labelType;
    /**
     * 物流产品代码 递四方必填
     */
    private LogisticsSaleChannelEntity logisticsSaleChannelEntity;

    //打印配货单(Y:打印;N:不打印)
    private String isPdn;

    //打印报关单(Y:打印;N:不打印)
    private String isPcd;
}
