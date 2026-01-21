package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 编辑公司DTO（新接口）
 * 对应新接口路径：/api/company/edit
 * 
 * @author system
 * @date 2025/01/XX
 */
@Data
public class EditCompanyDTO {

    /**
     * 税收类型（税务类型）
     */
    @NotBlank(message = "税收类型不能为空")
    @JsonProperty("invoice_type")
    private String invoiceType;

    /**
     * CNPJ
     */
    @NotBlank(message = "CNPJ不能为空")
    @JsonProperty("cnpj")
    private String cnpj;

    /**
     * 公司名称
     */
    @NotBlank(message = "公司名称不能为空")
    @JsonProperty("name")
    private String name;

    /**
     * 注册号（州税号）
     */
    @NotBlank(message = "注册号不能为空")
    @JsonProperty("ie")
    private String ie;

    /**
     * 公司单位类型（主公司或子公司）
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
     * 公司详细地址
     */
    @NotBlank(message = "公司详细地址不能为空")
    @JsonProperty("address")
    private String address;

    /**
     * 门牌号
     */
    @NotBlank(message = "门牌号不能为空")
    @JsonProperty("house_number")
    private String houseNumber;

    /**
     * 区镇（Bairro）
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
     * A1证书文件地址
     */
    @NotBlank(message = "证书文件地址不能为空")
    @JsonProperty("cert_file")
    private String certFile;

    /**
     * A1证书密码
     */
    @NotBlank(message = "证书密码不能为空")
    @JsonProperty("cert_pwd")
    private String certPwd;

    /**
     * 发票序列号（Número de série da NF-e）
     */
    @NotNull(message = "发票序列号不能为空")
    @JsonProperty("serie")
    private Integer serie;

    /**
     * 发票起始编号（NF-e começar Número）
     */
    @NotNull(message = "发票起始编号不能为空")
    @JsonProperty("number")
    private Integer number;

    /**
     * 公司ID（第三方返回的公司ID，编辑公司时必填）
     */
    @NotBlank(message = "公司ID不能为空")
    @JsonProperty("company_id")
    private String companyId;
}
