package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name = "address")
    private String address;

    /**
     *地址2
     */
    @JSONField(name = "address2")
    private String address2;

    /**
     *城市
     */
    @JSONField(name = "city")
    private String city;

    /**
     *收件人
     */
    @JSONField(name = "contact_person")
    private String contactPerson;

    /**
     *国家/地区
     */
    @JSONField(name = "country")
    private String country;

    /**
     *街道详细地址
     */
    @JSONField(name = "detail_address")
    private String detailAddress;


    /**
     *传真号
     */
    @JSONField(name = "fax_area")
    private String faxArea;

    /**
     *手机号
     */
    @JSONField(name = "mobile_no")
    private String mobileNo;

    /**
     *区号
     */
    @JSONField(name = "phone_area")
    private String phoneArea;

    /**
     *国家/地区码
     */
    @JSONField(name = "phone_country")
    private String phoneCountry;

    /**
     *电话
     */
    @JSONField(name = "phone_number")
    private String phoneNumber;

    /**
     *省份
     */
    @JSONField(name = "province")
    private String province;


    /**
     *邮编
     */
    @JSONField(name = "zip")
    private String zip;

    /**
     *巴西个人税号
     */
    @JSONField(name = "cpf_no")
    private String cpfNo;


}
