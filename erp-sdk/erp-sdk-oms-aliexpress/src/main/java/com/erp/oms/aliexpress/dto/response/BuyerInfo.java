package com.erp.oms.aliexpress.dto.response;

import com.google.gson.annotations.SerializedName;
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
    @SerializedName("country")
    private String country;

    /**
     *first name
     */
    @SerializedName("first_name")
    private String firstName;

    /**
     *
     * last name
     */
    @SerializedName("last_name")
    private String lastName;


    /**
     *
     * 登陆id
     */
    @SerializedName("login_id")
    private String loginId;


}
