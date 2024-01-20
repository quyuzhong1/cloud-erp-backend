package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.SerializedName;
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
    @SerializedName("product_id")
    private Long productId;
}
