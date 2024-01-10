package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 买家订单物流地址信息
 * @author Lambda
 * @Classname BuyerTradeAddress
 * @Description TODO
 * @Date 2024-01-10 14:30
 * @Created by yl
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class BuyerTradeAddress implements Serializable {


    /**
     * 妥买家全名
     */
    @JSONField(name = "buyer_signer_fullname")
    private String  buyerSignerFullname;

    /**
     * 电话号码
     */
    @JSONField(name = "phone_number")
    private String  phoneNumber;


    /**
     * address2
     */
    @JSONField(name = "address2")
    private String  address2;

    /**
     * 收件人
     */
    @JSONField(name = "contact_person")
    private String  contactPerson;

    /**
     * 收件详细地址
     */
    @JSONField(name = "detail_address")
    private String  detailAddress;

    /**
     * 收件详细地址
     */
    @JSONField(name = "mobile_no")
    private String  mobileNo;

    /**
     * 买家 first_name
     */
    @JSONField(name = "first_name")
    private String  firstName;
}
