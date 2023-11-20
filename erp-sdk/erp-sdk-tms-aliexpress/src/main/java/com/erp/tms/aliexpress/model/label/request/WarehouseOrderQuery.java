package com.erp.tms.aliexpress.model.label.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName WarehouseOrderQuery
 * @description: TODO
 * @date 2023年11月17日
 * @version: 1.0
 */
@Data
public class WarehouseOrderQuery implements Serializable {
    /**
     *
     * international logistics waybill ID
     * 是
     */
    @JSONField(name = "international_logistics_id")
    private String international_logistics_id;
    /**
     *
     * id is optional
     * 是
     */
    @JSONField(name = "id")
    private Long id;
}
