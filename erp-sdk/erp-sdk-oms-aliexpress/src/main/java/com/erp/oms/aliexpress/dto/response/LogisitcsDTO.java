package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname LogisitcsDTO
 * @Description TODO
 * @Date 2024-01-05 16:54
 * @Created by yl
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class LogisitcsDTO implements Serializable {

    /**
     * 妥投时间（此时间为美国太平洋时间）
     */
    @JSONField(name = "gmt_received")
    private String  gmtReceived;


    /**
     * 发货时间（此时间为美国太平洋时间）
     */
    @JSONField(name = "gmt_send")
    private String  gmtSend;

    /**
     * 是否可获取物流追踪信息
     */
    @JSONField(name = "have_tracking_info")
    private Boolean  haveTrackingInfo;

    /**
     *
     * 物流追踪号
     */
    @JSONField(name = "logistics_no")
    private String  logisticsNo;

    /**
     *
     * 发货物流服务展示线路英文名称
     */
    @JSONField(name = "logistics_service_name")
    private String  logisticsServiceName;

    /**
     *
     * 物流服务名称key(与声明发货中的service_name相同)
     */
    @JSONField(name = "logistics_type_code")
    private String  logisticsTypeCode;

    /**
     * 投递状态。
     * (default:初始值; received:已经妥投; not_received:没有妥投; suspected_received:疑似妥投)
     */
    @JSONField(name = "receive_status")
    private String  receiveStatus;

    /**
     * 未妥投原因，如国家/地区不匹配
     */
    @JSONField(name = "recv_status_desc")
    private String  recvStatusDesc;

    /**
     * 物流订单ID
     */
    @JSONField(name = "ship_order_id")
    private String  shipOrderId;




}
