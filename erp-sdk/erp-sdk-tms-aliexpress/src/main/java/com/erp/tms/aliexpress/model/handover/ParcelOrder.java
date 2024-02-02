package com.erp.tms.aliexpress.model.handover;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ParcelOrder
 * @description: 大包关联的小包列表
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
public class ParcelOrder implements Serializable {
    /**
     * 小包物流订单编码
     */
    @JSONField(name = "order_code")
    private String orderCode;
    /**
     * 小包状态code
     */
    @JSONField(name = "status")
    private String status;
    /**
     * 小包异常码
     */
    @JSONField(name = "exception_code")
    private String exceptionCode;
    /**
     * 小包状态名称
     */
    @JSONField(name = "status_name")
    private String statusName;
}
