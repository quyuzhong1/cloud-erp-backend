package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ServiceResponse
 * @description: 描述：用于异常订单重新发货时获取物流方案，如异常滞留订单
 * @date 2024年02月05日
 * @version: 1.0
 */
@Data
public class ServiceResponse implements Serializable {
    /**
     * 物流方案code
     */
    @JSONField(name = "solution_code")
    private String solutionCode;
    /**
     * 物流方案name
     */
    @JSONField(name = "solution_name")
    private String solutionName;
    /**
     * 预计时效
     */
    @JSONField(name = "estimate_time")
    private String estimateTime;
    /**
     * 预计新费用(CNY)
     */
    @JSONField(name = "new_estimate_fee")
    private String newEstimateFee;
    /**
     * 预计原费用(CNY)
     */
    @JSONField(name = "original_estimate_fee")
    private String originalEstimateFee;
}
