package com.erp.tms.aliexpress.model.handover;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName SlaveBigbag
 * @description: 批次约揽下的子大包列表
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
public class SlaveBigbag implements Serializable {

    /**
     * 交接物物流订单编号
     */
    @JSONField(name = "order_code")
    private String orderCode;

    /**
     * 交接物运单号
     */
    @JSONField(name = "tracking_number")
    private String trackingNumber;
    /**
     * 状态
     */
    @JSONField(name = "status")
    private String status;
    /**
     * 状态名
     */
    @JSONField(name = "status_name")
    private String statusName;
    /**
     * 揽收失败时的实操回传备注
     */
    @JSONField(name = "op_remark")
    private String opRemark;
    /**
     * 交接id
     */
    @JSONField(name = "order_content_id")
    private Long orderContentId;
    /**
     * 交接物运单号
     */
    @JSONField(name = "gmt_create")
    private String gmtCreate;
}
