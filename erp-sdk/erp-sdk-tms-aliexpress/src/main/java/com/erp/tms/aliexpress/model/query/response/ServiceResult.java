package com.erp.tms.aliexpress.model.query.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ServiceResult
 * @description: TODO
 * @date 2023年12月29日
 * @version: 1.0
 */
@Data
public class ServiceResult implements Serializable {
    /**
     * 服务名
     */
    @JSONField(name = "logistics_service_name")
    private String logisticsServiceName;
    /**
     * 是否过期
     */
    @JSONField(name = "is_express_logistics_service")
    private Boolean isExpressLogisticsService;
    /**
     * 交货地址
     */
    @JSONField(name = "delivery_address")
    private String deliveryAddress;
    /**
     * 仓库中文名称
     */
    @JSONField(name = "warehouse_name")
    private String warehouseName;
    /**
     *试算结果
     */
    @JSONField(name = "trial_result")
    private String trialResult;
    /**
     *运输时效
     */
    @JSONField(name = "logistics_timeliness")
    private String logisticsTimeliness;
    /**
     *折扣试算结果
     */
    @JSONField(name = "discount_trial_result")
    private String discountTrialResult;
    /**
     *物流方案ID
     */
    @JSONField(name = "logistics_service_id")
    private String logisticsServiceId;
    /**
     *推荐指数
     */
    @JSONField(name = "recommend_index")
    private String recommend_index;
}
