package com.erp.oms.aliexpress.dto.response;/**
 * @author Lambda
 * @Classname AmountInfo
 * @Description TODO
 * @Date 2023-11-29 11:44
 * @Created by yl
 */

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-29 11:44
 */
@Data
public class AmountInfo implements Serializable {


    /**
     * 金额
     */
    @SerializedName("amount")
    private String amount;

    /**
     * 币别
     */
    @SerializedName("currency_code")
    private String currencyCode;

    /**
     * 乘积因子
     */
    @SerializedName("cent_factor")
    private Integer  centFactor;


}
