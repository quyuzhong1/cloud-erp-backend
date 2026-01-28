package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建公司DTO（新接口）
 * 对应新接口路径：/company/create
 * 
 * @author system
 * @date 2025/01/XX
 */
@Data
public class CreateCompanyDTO {

    /**
     * 税收类型（税务类型）
     * 取值新建发票填写的税务类型
     */
    @NotBlank(message = "税收类型不能为空")
    @JsonProperty("invoice_type")
    private String invoiceType;

    /**
     * 公司名称
     */
    @NotBlank(message = "公司名称不能为空")
    @JsonProperty("name")
    private String name;

    /**
     * CNPJ
     */
    @NotBlank(message = "CNPJ不能为空")
    @JsonProperty("cnpj")
    private String cnpj;

    /**
     * 注册号（州税号）
     */
    @NotBlank(message = "注册号不能为空")
    @JsonProperty("ie")
    private String ie;

    /**
     * 公司单位类型（主公司或子公司）
     * 取值选择的公司类型（dict_company_type）
     */
    @NotBlank(message = "公司单位类型不能为空")
    @JsonProperty("unit")
    private String unit;

    /**
     * 公司邮箱
     */
    @NotBlank(message = "公司邮箱不能为空")
    @JsonProperty("email")
    private String email;

    /**
     * 公司邮编
     */
    @NotBlank(message = "公司邮编不能为空")
    @JsonProperty("cep")
    private String cep;

    /**
     * 公司详细地址（原rua）
     */
    @NotBlank(message = "公司详细地址不能为空")
    @JsonProperty("address")
    private String address;

    /**
     * 门牌号（原numero）
     */
    @NotBlank(message = "门牌号不能为空")
    @JsonProperty("house_number")
    private String houseNumber;

    /**
     * 区镇（Bairro，原bairro）
     */
    @NotBlank(message = "区镇不能为空")
    @JsonProperty("town")
    private String town;

    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    @JsonProperty("city")
    private String city;

    /**
     * 州
     */
    @NotBlank(message = "州不能为空")
    @JsonProperty("state")
    private String state;

    /**
     * 证书文件地址（A1证书链接，原certificado）
     */
    @NotBlank(message = "证书文件地址不能为空")
    @JsonProperty("cert_file")
    private String certFile;

    /**
     * 证书密码（A1证书密码，原senhaCertificado）
     */
    @NotBlank(message = "证书密码不能为空")
    @JsonProperty("cert_pwd")
    private String certPwd;

    /**
     * 发票序列号（原numeroSerieNfe）
     */
    @NotBlank(message = "发票序列号不能为空")
    @JsonProperty("serie")
    private String serie;

    /**
     * 发票起始编号（原ultimoNumeroNfe）
     */
    @NotBlank(message = "发票起始编号不能为空")
    @JsonProperty("number")
    private String number;

    /**
     * 税种ID
     * 取值税种查询接口返回的category_id
     */
    @NotBlank(message = "税种ID不能为空")
    @JsonProperty("category_id")
    private String categoryId;
}
