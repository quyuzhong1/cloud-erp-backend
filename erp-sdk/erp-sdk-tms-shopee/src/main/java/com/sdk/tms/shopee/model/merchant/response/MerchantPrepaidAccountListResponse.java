package com.sdk.tms.shopee.model.merchant.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Shopee卖家月结账号列表响应。
 */
@Data
public class MerchantPrepaidAccountListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "list")
    private List<MerchantPrepaidAccount> list;

    private Boolean more;

    private Integer total;
}
