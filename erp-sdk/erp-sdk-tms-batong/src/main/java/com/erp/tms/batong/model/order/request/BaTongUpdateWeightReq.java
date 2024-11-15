package com.erp.tms.batong.model.order.request;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname OrderRequest
 * @Description 更新重量
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaTongUpdateWeightReq implements Serializable {

    /**
     * 客户参考号
     */
    @Alias("reference_no")
    private String referenceNo;

    /**
     * 订单重量，单位KG
     */
    @Alias("order_weight")
    private String orderWeight;

}
