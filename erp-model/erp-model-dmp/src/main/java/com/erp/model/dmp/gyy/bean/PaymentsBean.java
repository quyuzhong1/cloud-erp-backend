package com.erp.model.dmp.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@ToString
public class PaymentsBean {
    /**
     * payment : 85.67
     * payCode : 2022111022001160975737046693
     * account : 182****9727
     * pay_type_name : 支付宝
     * paytime : 2022-11-10 20:12:12
     */

    @SerializedName("payment")
    private BigDecimal payment;
    @SerializedName("payCode")
    private String payCode;
    @SerializedName("account")
    private String account;
    @SerializedName("pay_type_name")
    private String payTypeName;
    @SerializedName("paytime")
    private String paytime;

}
