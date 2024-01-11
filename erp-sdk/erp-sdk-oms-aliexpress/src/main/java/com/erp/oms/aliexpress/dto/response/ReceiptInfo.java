package com.erp.oms.aliexpress.dto.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname ReceiptInfo
 * @Description 收货地址信息
 * @Date 2023-12-01 14:59
 * @Created by yl
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class ReceiptInfo  implements Serializable {


    /**
     *地址1
     */
    @SerializedName("address")
    private String address;

    /**
     *地址2
     */
    @SerializedName("address2")
    private String address2;

    /**
     *城市
     */
    @SerializedName("city")
    private String city;

    /**
     *收件人
     */
    @SerializedName("contact_person")
    private String contactPerson;

    /**
     *国家/地区
     */
    @SerializedName("country")
    private String country;

    /**
     *街道详细地址
     */
    @SerializedName("detail_address")
    private String detailAddress;


    /**
     *传真号
     */
    @SerializedName("fax_area")
    private String faxArea;

    /**
     *手机号
     */
    @SerializedName("mobile_no")
    private String mobileNo;

    /**
     *区号
     */
    @SerializedName("phone_area")
    private String phoneArea;

    /**
     *国家/地区码
     */
    @SerializedName("phone_country")
    private String phoneCountry;

    /**
     *电话
     */
    @SerializedName("phone_number")
    private String phoneNumber;

    /**
     *省份
     */
    @SerializedName("province")
    private String province;


    /**
     *邮编
     */
    @SerializedName("zip")
    private String zip;

    /**
     *巴西个人税号
     */
    @SerializedName("cpf_no")
    private String cpfNo;


}
