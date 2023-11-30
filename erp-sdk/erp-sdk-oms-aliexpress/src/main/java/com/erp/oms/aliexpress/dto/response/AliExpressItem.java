package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname AliExpressItem
 * @Description TODO
 * @Date 2023-11-30 14:13
 * @Created by yl
 */
@Data
public class AliExpressItem implements Serializable {
    @JSONField(name = "product_id")
    private Long productId;
}
