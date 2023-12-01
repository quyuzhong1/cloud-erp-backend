package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname BuyerInfo
 * @Description TODO
 * @Date 2023-12-01 15:01
 * @Created by yl
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class BuyerInfo implements Serializable {

    /**
     *国家/地区
     */
    @JSONField(name = "country")
    private String country;

    /**
     *first name
     */
    @JSONField(name = "first_name")
    private String firstName;

    /**
     *
     * last name
     */
    @JSONField(name = "last name")
    private String lastName;


}
