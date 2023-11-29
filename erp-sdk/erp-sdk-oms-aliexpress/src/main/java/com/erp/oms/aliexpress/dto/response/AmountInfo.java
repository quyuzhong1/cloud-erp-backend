package com.erp.oms.aliexpress.dto.response;/**
 * @author Lambda
 * @Classname AmountInfo
 * @Description TODO
 * @Date 2023-11-29 11:44
 * @Created by yl
 */

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name = "amount")
    private String amount;

    /**
     * 币别
     */
    @JSONField(name = "currency_code")
    private String currencyCode;


}
