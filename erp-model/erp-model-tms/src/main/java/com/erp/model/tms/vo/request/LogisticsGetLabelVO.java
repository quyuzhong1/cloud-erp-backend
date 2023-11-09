package com.erp.model.tms.vo.request;

import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import lombok.Data;

import java.io.Serializable;

@Data
public class LogisticsGetLabelVO extends LogisticsQueryBaseVO implements Serializable {
    //打印拣货单
    private Integer printRemark;
    //
    private String labelType;
    /**
     * 物流产品代码 递四方必填
     */
    private LogisticsChannelEntity logisticsChannelEntity;
}
