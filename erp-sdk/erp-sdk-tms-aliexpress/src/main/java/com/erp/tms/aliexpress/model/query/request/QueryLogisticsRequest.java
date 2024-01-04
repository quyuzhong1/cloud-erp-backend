package com.erp.tms.aliexpress.model.query.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName QueryLogisticsRequest
 * @description: TODO
 * @date 2023年12月26日
 * @version: 1.0
 */
@Data
@Builder
public class QueryLogisticsRequest implements Serializable {
    @JSONField(name = "order_id")
    private Long order_id;
    @JSONField(name = "goods_length")
    private Long goods_length;
    @JSONField(name = "goods_weight")
    private String goods_weight;
    @JSONField(name = "goods_height")
    private Long goods_height;
    @JSONField(name = "goods_width")
    private Long goods_width;
    @JSONField(name = "locale")
    private String locale;
    @JSONField(name = "sub_order_list")
    private List<QueryLogisticsRequest> sub_order_list;
}
