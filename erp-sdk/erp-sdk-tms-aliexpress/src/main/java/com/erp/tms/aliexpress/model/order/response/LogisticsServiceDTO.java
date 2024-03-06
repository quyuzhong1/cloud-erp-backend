package com.erp.tms.aliexpress.model.order.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname LogisticsService
 * @Description TODO
 * @Date 2024-03-04 15:54
 * @Created by yl
 */
@Data
public class LogisticsServiceDTO implements Serializable {

    /**
     * 推荐显示排序
     */
    @JSONField(name = "recommend_order")
    private Integer recommendOrder;

    /**
     * 物流追踪号码校验规则，采用正则表达
     */
    @JSONField(name = "tracking_no_regex")
    private String trackingNoRegex;

    /**
     * 最小处理时间
     */
    @JSONField(name = "min_process_day")
    private Integer minProcessDay;
    /**
     * 物流公司
     */
    @JSONField(name = "logistics_company")
    private String logisticsCompany;

    /**
     * 最大处理时间
     */
    @JSONField(name = "max_process_day")
    private Integer maxProcessDay;

    /**
     * 展示名称
     */
    @JSONField(name = "display_name")
    private String displayName;

    /**
     * 物流服务key
     */
    @JSONField(name = "service_name")
    private String serviceName;



}
