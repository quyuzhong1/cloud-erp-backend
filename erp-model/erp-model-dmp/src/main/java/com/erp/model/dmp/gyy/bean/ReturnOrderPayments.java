package com.erp.model.dmp.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /**
     * 退款金额
     */
    @SerializedName("payment")
    private BigDecimal payment;
    /**
     * 退款账号
     */
    @SerializedName("account")
    private String account;
    /**
     * 备注
     */
    @SerializedName("note")
    private String note;
    /**
     * 退款方式代码
     */
    @SerializedName("pay_type_code")
    private String payTypeCode;
    /**
     * 退款时间
     */
    @SerializedName("pay_time")
    private LocalDateTime payTime;
}
