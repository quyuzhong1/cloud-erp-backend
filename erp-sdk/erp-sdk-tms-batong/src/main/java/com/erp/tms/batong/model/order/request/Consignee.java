package com.erp.tms.batong.model.order.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname consignee
 * @Description 收件人信息
 * @Date 2024-01-12 14:30
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consignee  implements Serializable {

    /**
     * 收件人姓名
     */
    @NotBlank(message = "收件人姓名不能为空")
    @JSONField(name = "consignee_name")
    private String consigneeName;

    /**
     * 收件人公司名
     */
    @JSONField(name = "consignee_company")
    private String consigneeCompany;

    /**
     * 收件人国家二字代码
     */
    @NotBlank(message = "收件人国家二字代码不能为空")
    @JSONField(name = "consignee_countrycode")
    private String consigneeCountryCode;

    /**
     * 收件人州/省
     */
    @JSONField(name = "consignee_province")
    private String consigneeProvince;


    /**
     * 收件人城市
     */
    @JSONField(name = "consignee_city")
    private String consigneeCity;


    /**
     * 收件人区/县
     */
    @JSONField(name = "consignee_name")
    private String consigneeDistrict;


    /**
     * 收件人街道地址
     */
    @NotBlank(message = "收件人街道地址不能为空")
    @JSONField(name = "consignee_street")
    private String consigneeStreet;


    /**
     * 收件人邮编
     */
    @JSONField(name = "consignee_postcode")
    private String consigneePostCode;


    /**
     * 收件人门牌号
     */
    @JSONField(name = "consignee_doorplate")
    private String consigneeDoorplate;

    /**
     * 收件人区域代码
     */
    @JSONField(name = "consignee_areacode")
    private String consigneeAreaCode;

    /**
     * 收件人电话
     */
    @JSONField(name = "consignee_telephone")
    private String consigneeTelephone;

    /**
     * 收件人手机
     */
    @JSONField(name = "consignee_mobile")
    private String consigneeMobile;

    /**
     * 收件人邮箱
     */
    @JSONField(name = "consignee_email")
    private String consigneeEmail;

    /**
     * 收件人传真
     */
    @JSONField(name = "consignee_fax")
    private String consigneeFax;

    /**
     * 证件类型代码
     * ID：身份证
     * PP：护照
     */
    @JSONField(name = "consignee_certificatetype")
    private String consigneeCertificateType;

    /**
     * 证件号码
     */
    @JSONField(name = "consignee_certificatecode")
    private String consigneeCertificateCode;

    /**
     * 证件有效期
     */
    @JSONField(name = "consignee_credentials_period")
    private String consigneeCredentialsPeriod;


    /**
     * 收件人税号
     */
    @JSONField(name = "consignee_tariff")
    private String consigneeTariff;

}
