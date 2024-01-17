package com.erp.oms.aliexpress.dto.response;

import com.google.gson.annotations.SerializedName;
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
    @SerializedName("buyer_signer_fullname")
    private String  buyerSignerFullname;

    /**
     * 电话号码
     */
    @SerializedName("phone_number")
    private String  phoneNumber;


    /**
     * address2
     */
    @SerializedName("address2")
    private String  address2;

    /**
     * 收件人
     */
    @SerializedName("contact_person")
    private String  contactPerson;

    /**
     * 收件详细地址
     */
    @SerializedName("detail_address")
    private String  detailAddress;

    /**
     * 收件详细地址
     */
    @SerializedName("mobile_no")
    private String  mobileNo;

    /**
     * 买家 first_name
     */
    @SerializedName("first_name")
    private String  firstName;
}
