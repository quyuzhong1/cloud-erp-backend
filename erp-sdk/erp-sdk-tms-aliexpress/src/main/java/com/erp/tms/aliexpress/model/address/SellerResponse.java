package com.erp.tms.aliexpress.model.address;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.tms.aliexpress.model.order.request.Address;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName SellerResponse
 * @description: TODO
 * @date 2023年12月29日
 * @version: 1.0
 */
@Data
public class SellerResponse implements Serializable {
    @JSONField(name = "sender_seller_address_list")
    private List<Address> senders;
    @JSONField(name = "refund_seller_address_list")
    private List<Address> refunds;
    @JSONField(name = "pickup_seller_address_list")
    private List<Address> pickups;
    @JSONField(name = "error_desc")
    private String error_desc;
    @JSONField(name = "result_error_code")
    private Long result_error_code;
    @JSONField(name = "result_success")
    private Boolean result_success;
}
