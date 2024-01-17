package com.erp.tms.batong.model.order.request;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname Shipper
 * @Description 发件人信息
 * @Date 2024-01-12 14:14
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipper implements Serializable {

    /**
     * 发件人姓名
     */
    @NotBlank(message = "发件人姓名不能为空")
    @Alias("shipper_name")
    private String shipperName;


    /**
     * 发件人公司
     */
    @Alias("shipper_company")
    private String shipperCompany;


    /**
     * 发件人国家二字代码
     */
    @Alias("shipper_countrycode")
    @NotBlank(message = "发件人国家二字代码不能为空")
    private String shipperCountryCode;

    /**
     * 发件人州/省
     */
    @Alias("shipper_province")
    private String shipperProvince;


    /**
     * 发件人城市
     */
    @Alias("shipper_city")
    private String shipperCity;

    /**
     * 发件人城市
     */
    @Alias("shipper_district")
    private String shipperDistrict;

    /**
     * 发件人街道地址
     */
    @Alias("shipper_street")
    @NotBlank(message = "发件人街道地址不能为空")
    private String shipperStreet;

    /**
     * 发件人邮编
     */
    @Alias("shipper_postcode")
    private String shipperPostCode;

    /**
     * 发件人区域代码
     */
    @Alias("shipper_areacode")
    private String shipperAreaCode;

    /**
     * 发件人电话
     */
    @Alias("shipper_telephone")
    private String shipperTelephone;

    /**
     * 发件人手机
     */
    @Alias("shipper_mobile")
    private String shipperMobile;

    /**
     * 发件人邮箱
     */
    @Alias("shipper_email")
    private String shipperEmail;
    /**
     * 发件人传真
     */
    @Alias("shipper_fax")
    private String shipperFax;


}
