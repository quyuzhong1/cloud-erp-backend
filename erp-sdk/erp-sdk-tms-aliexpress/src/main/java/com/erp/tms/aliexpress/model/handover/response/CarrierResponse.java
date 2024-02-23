package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName CarrierResponse
 * @description: 描述：查询出所有的实际承运商
 * @date 2024年02月05日
 * @version: 1.0
 */
@Data
public class CarrierResponse implements Serializable {
    /**
     * 承运商名字
     */
    @JSONField(name = "courier_name")
    private String courierName;
    /**
     *
     * 承运商code
     */
    @JSONField(name = "courier_code")
    private String courierCode;
}
