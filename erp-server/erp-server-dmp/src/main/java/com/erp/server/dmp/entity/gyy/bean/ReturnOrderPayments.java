package com.erp.server.dmp.entity.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@NoArgsConstructor
public class ReturnOrderPayments {
    /**
     * payment : 292.0
     * account : 2021020522001100261434628493
     * note : null
     * pay_type_code : zhifubao
     * pay_time : null
     */

    @SerializedName("payment")
    private BigDecimal payment;
    @SerializedName("account")
    private String account;
    @SerializedName("note")
    private String note;
    @SerializedName("pay_type_code")
    private String payTypeCode;
    @SerializedName("pay_time")
    private String payTime;
}
