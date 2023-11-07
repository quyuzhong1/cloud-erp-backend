package com.erp.model.tms.vo.request;

import com.erp.model.tms.entity.LogisticsAuthEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsQueryVO
 * @description: 查询类
 * @date 2023年11月06日
 * @version: 1.0
 */
@Data
public class LogisticsQueryBaseVO implements Serializable {
    /**
     * 发货单号
     */
    private String deliveryNo;
    /**
     * 运单号
     */
    String transportNo;
    /**
     * 跟踪号
     */
    List<String> trackNoList;
    /**
     * 授权信息
     */
    LogisticsAuthEntity logisticsAuthEntity;

}
