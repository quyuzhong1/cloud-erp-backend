package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 开具发票请求DTO（新接口）
 * 对应新接口路径：/api/invoice/create
 * 
 * @author system
 * @date 2025/01/XX
 */
@Data
public class CreateInvoiceDTO implements Serializable {

    /**
     * 发票ID（建议使用订单ID，不超过15字符）
     */
    @NotBlank(message = "发票ID不能为空")
    @JsonProperty("id")
    private String id;

    /**
     * 是否重开发票
     */
    @JsonProperty("is_reopen")
    @JsonAlias({"is_reopen", "isReopen"})
    private Boolean isReopen;

    /**
     * 业务性质（例如：销售、退货等）
     */
    @NotBlank(message = "业务性质不能为空")
    @JsonProperty("nature_of_operation")
    @JsonAlias({"nature_of_operation", "natureOfOperation"})
    private String natureOfOperation;

    /**
     * 交易类型（例如：1-出项发票，2-进项发票等）
     */
    @NotBlank(message = "交易类型不能为空")
    @JsonProperty("transaction_type")
    @JsonAlias({"transaction_type", "transactionType"})
    private String transactionType;

    /**
     * 发票模式（例如：55-NFe, 65-NFCe）
     */
    @NotBlank(message = "发票模式不能为空")
    @JsonProperty("model")
    private String model;

    /**
     * 发票类型（例如：1-正常）
     */
    @NotBlank(message = "发票类型不能为空")
    @JsonProperty("issuance_type")
    @JsonAlias({"issuance_type", "issuanceType"})
    private String issuanceType;

    /**
     * 运行环境（1-生产，2-测试）
     */
    @NotBlank(message = "运行环境不能为空")
    @JsonProperty("ambiente")
    @JsonAlias({"ambiente", "Ambiente"})
    private String ambiente;

    /**
     * 总折扣金额（包含在发票总金额中）
     * 注意：根据API规范，该字段不传
     */
    @JsonProperty("total_discount_amount")
    @JsonAlias({"total_discount_amount", "totalDiscountAmount"})
    private BigDecimal totalDiscountAmount;

    /**
     * 客户信息
     */
    @NotNull(message = "客户信息不能为空")
    @Valid
    @JsonProperty("cliente")
    private ClienteDTO cliente;

    /**
     * 商品列表
     */
    @NotEmpty(message = "商品列表不能为空")
    @Valid
    @JsonProperty("products")
    private List<ProductDTO> products;

    /**
     * 运输信息
     */
    @NotNull(message = "运输信息不能为空")
    @Valid
    @JsonProperty("transportation")
    private TransportationDTO transportation;

    /**
     * 发货地址（可选）
     */
    @JsonProperty("shipping_address")
    @JsonAlias({"shipping_address", "shippingAddress"})
    private AddressDTO shippingAddress;

    /**
     * 收货地址（可选）
     */
    @JsonProperty("delivery_address")
    @JsonAlias({"delivery_address", "deliveryAddress"})
    private AddressDTO deliveryAddress;

    /**
     * 支付信息（可选）
     */
    @JsonProperty("pag_info")
    private List<PaymentInfoDTO> pagInfo;

    /**
     * 客户信息DTO
     */
    @Data
    public static class ClienteDTO implements Serializable {
        /**
         * 客户名称
         */
        @NotBlank(message = "客户名称不能为空")
        @JsonProperty("name")
        private String name;

        /**
         * 个人CPF号码（如果是个人客户）
         */
        @JsonProperty("cpf")
        private String cpf;

        /**
         * 公司CNPJ号码（如果是企业客户）
         */
        @JsonProperty("cnpj")
        private String cnpj;

        /**
         * 注册号（Inscrição Estadual）
         */
        @JsonProperty("ie")
        private String ie;

        /**
         * 地址
         */
        @JsonProperty("endereco")
        private String endereco;

        /**
         * 门牌号（无门牌号时填入S/N）
         */
        @NotBlank(message = "门牌号不能为空")
        @JsonProperty("numero")
        private String numero;

        /**
         * 区/镇
         */
        @NotBlank(message = "区镇不能为空")
        @JsonProperty("bairro")
        private String bairro;

        /**
         * 城市
         */
        @NotBlank(message = "城市不能为空")
        @JsonProperty("city")
        private String city;

        /**
         * 州（例如：SP, RJ）
         */
        @JsonProperty("uf")
        private String uf;

        /**
         * 邮政编码（8位纯数字）
         */
        @NotBlank(message = "邮编不能为空")
        @JsonProperty("cep")
        private String cep;

        /**
         * 联系电话
         */
        @JsonProperty("telefone")
        private String telefone;

        /**
         * 电子邮箱
         */
        @JsonProperty("email")
        private String email;
    }

    /**
     * 商品信息DTO
     */
    @Data
    public static class ProductDTO implements Serializable {
        /**
         * 商品名称
         */
        @NotBlank(message = "商品名称不能为空")
        @JsonProperty("name")
        private String name;

        /**
         * 商品代码（SKU编码）
         */
        @NotBlank(message = "商品代码不能为空")
        @JsonProperty("codigo")
        private String codigo;

        /**
         * 税务商品编码（NCM，8位纯数字）
         */
        @NotBlank(message = "税务商品编码不能为空")
        @JsonProperty("ncm")
        private String ncm;

        /**
         * 数量
         */
        @NotBlank(message = "数量不能为空")
        @JsonProperty("count")
        private String count;

        /**
         * 单位（例如：UN, KG, CX, PCT, SC）
         */
        @NotBlank(message = "单位不能为空")
        @JsonProperty("unit")
        private String unit;

        /**
         * 商品GTIN码
         */
        @JsonProperty("gtin")
        private String gtin;

        /**
         * 计税GTIN
         */
        @JsonProperty("gtin_tributavel")
        @JsonAlias({"gtin_tributavel", "gtinTributavel"})
        private String gtinTributavel;

        /**
         * 单价
         */
        @NotNull(message = "单价不能为空")
        @JsonProperty("unit_price")
        @JsonAlias({"unit_price", "unitPrice"})
        private BigDecimal unitPrice;

        /**
         * 总价
         */
        @NotNull(message = "总价不能为空")
        @JsonProperty("total_price")
        @JsonAlias({"total_price", "totalPrice"})
        private BigDecimal totalPrice;

        /**
         * 折扣价格
         * 注意：根据API规范，当值为0或null时，该字段不传
         */
        @JsonProperty("discount_price")
        @JsonAlias({"discount_price", "discountPrice"})
        private BigDecimal discountPrice;

        /**
         * 税种ID（当存在值时impostos字段不进行传参）
         */
        @JsonProperty("category_id")
        @JsonAlias({"category_id", "categoryId"})
        private String categoryId;

        /**
         * 税配置信息（当category_id值为不传时必填）
         */
        @JsonProperty("tax_info")
        @JsonAlias({"tax_info", "taxInfo"})
        private TaxInfoDTO taxInfo;

        /**
         * 商品来源（0-Nacional国内商品）
         */
        @NotBlank(message = "商品来源不能为空")
        @JsonProperty("origem")
        private String origem;

        /**
         * 是否包含在发票总额中（1-是，0-否）默认1
         */
        @NotBlank(message = "是否包含在发票总额中不能为空")
        @JsonProperty("indicador_total")
        @JsonAlias({"indicador_total", "indicadorTotal"})
        private String indicadorTotal;

        /**
         * 进口报关单（当transaction_type为2时必填）
         */
        @JsonProperty("import_declaration")
        @JsonAlias({"import_declaration", "importDeclaration"})
        private ImportDeclarationDTO importDeclaration;
    }

    /**
     * 税配置信息DTO
     */
    @Data
    public static class TaxInfoDTO implements Serializable {
        /**
         * ICMS税信息
         */
        @JsonProperty("icms")
        private IcmsDTO icms;

        /**
         * IPI税信息
         */
        @JsonProperty("ipi")
        private IpiDTO ipi;

        /**
         * PIS税信息
         */
        @JsonProperty("pis")
        private PisDTO pis;

        /**
         * COFINS税信息
         */
        @JsonProperty("cofins")
        private CofinsDTO cofins;
    }

    /**
     * ICMS税信息DTO
     */
    @Data
    public static class IcmsDTO implements Serializable {
        /**
         * 人员类型（fisica-自然人, juridica-法人）
         */
        @JsonProperty("tipo_pessoa")
        @JsonAlias({"tipo_pessoa", "tipoPessoa"})
        private String tipoPessoa;

        /**
         * CFOP编码（Código Fiscal de Operações e Prestações）
         */
        @JsonProperty("codigo_cfop")
        @JsonAlias({"codigo_cfop", "codigoCfop"})
        private String codigoCfop;

        /**
         * 税务情形代码（CST或CSOSN）
         */
        @JsonProperty("situacao_tributaria")
        @JsonAlias({"situacao_tributaria", "situacaoTributaria"})
        private String situacaoTributaria;

        /**
         * ICMS税率（默认0.00）
         */
        @JsonProperty("aliquota")
        private String aliquota;
    }

    /**
     * IPI税信息DTO
     */
    @Data
    public static class IpiDTO implements Serializable {
        /**
         * 人员类型（fisica-自然人, juridica-法人）
         */
        @JsonProperty("tipo_pessoa")
        @JsonAlias({"tipo_pessoa", "tipoPessoa"})
        private String tipoPessoa;

        /**
         * IPI税务情形代码
         */
        @JsonProperty("situacao_tributaria")
        @JsonAlias({"situacao_tributaria", "situacaoTributaria"})
        private String situacaoTributaria;

        /**
         * 归类代码（Código de enquadramento，默认999）
         */
        @JsonProperty("codigo_enquadramento")
        @JsonAlias({"codigo_enquadramento", "codigoEnquadramento"})
        private String codigoEnquadramento;

        /**
         * IPI税率（默认0.00）
         */
        @JsonProperty("aliquota")
        private String aliquota;
    }

    /**
     * PIS税信息DTO
     */
    @Data
    public static class PisDTO implements Serializable {
        /**
         * 人员类型（fisica-自然人, juridica-法人）
         */
        @JsonProperty("tipo_pessoa")
        @JsonAlias({"tipo_pessoa", "tipoPessoa"})
        private String tipoPessoa;

        /**
         * PIS税务情形代码
         */
        @JsonProperty("situacao_tributaria")
        @JsonAlias({"situacao_tributaria", "situacaoTributaria"})
        private String situacaoTributaria;

        /**
         * PIS税率（默认0.00）
         */
        @JsonProperty("aliquota")
        private String aliquota;
    }

    /**
     * COFINS税信息DTO
     */
    @Data
    public static class CofinsDTO implements Serializable {
        /**
         * 人员类型（fisica-自然人, juridica-法人）
         */
        @JsonProperty("tipo_pessoa")
        @JsonAlias({"tipo_pessoa", "tipoPessoa"})
        private String tipoPessoa;

        /**
         * COFINS税务情形代码
         */
        @JsonProperty("situacao_tributaria")
        @JsonAlias({"situacao_tributaria", "situacaoTributaria"})
        private String situacaoTributaria;

        /**
         * COFINS税率（默认0.00）
         */
        @JsonProperty("aliquota")
        private String aliquota;
    }

    /**
     * 进口报关单DTO
     */
    @Data
    public static class ImportDeclarationDTO implements Serializable {
        // 根据实际需求添加字段
    }

    /**
     * 运输信息DTO
     */
    @Data
    public static class TransportationDTO implements Serializable {
        /**
         * 运输方式（默认为3）
         */
        @NotBlank(message = "运输方式不能为空")
        @JsonProperty("transport_mode")
        @JsonAlias({"transport_mode", "transportMode"})
        private String transportMode;

        /**
         * 运费金额（可为0）
         */
        @JsonProperty("freight_amount")
        @JsonAlias({"freight_amount", "freightAmount"})
        private BigDecimal freightAmount;

        /**
         * 运输公司信息（可选）
         */
        @JsonProperty("carrier_info")
        @JsonAlias({"carrier_info", "carrierInfo"})
        private CarrierInfoDTO carrierInfo;

        /**
         * 车辆信息（可选）
         */
        @JsonProperty("vehicle_info")
        @JsonAlias({"vehicle_info", "vehicleInfo"})
        private VehicleInfoDTO vehicleInfo;

        /**
         * 拖车信息（可选）
         */
        @JsonProperty("trailer_info")
        @JsonAlias({"trailer_info", "trailerInfo"})
        private TrailerInfoDTO trailerInfo;

        /**
         * 运输包裹信息（可选）
         */
        @JsonProperty("packages")
        private List<PackageDTO> packages;

        /**
         * 运输服务预扣ICMS信息（可选）
         */
        @JsonProperty("transport_tax_retention_info")
        @JsonAlias({"transport_tax_retention_info", "transportTaxRetentionInfo"})
        private TransportTaxRetentionInfoDTO transportTaxRetentionInfo;
    }

    /**
     * 地址DTO
     */
    @Data
    public static class AddressDTO implements Serializable {
        // 根据实际需求添加字段
    }

    /**
     * 支付信息DTO
     */
    @Data
    public static class PaymentInfoDTO implements Serializable {
        // 根据实际需求添加字段
    }

    /**
     * 运输公司信息DTO
     */
    @Data
    public static class CarrierInfoDTO implements Serializable {
        // 根据实际需求添加字段
    }

    /**
     * 车辆信息DTO
     */
    @Data
    public static class VehicleInfoDTO implements Serializable {
        // 根据实际需求添加字段
    }

    /**
     * 拖车信息DTO
     */
    @Data
    public static class TrailerInfoDTO implements Serializable {
        // 根据实际需求添加字段
    }

    /**
     * 包裹信息DTO
     */
    @Data
    public static class PackageDTO implements Serializable {
        // 根据实际需求添加字段
    }

    /**
     * 运输服务预扣ICMS信息DTO
     */
    @Data
    public static class TransportTaxRetentionInfoDTO implements Serializable {
        // 根据实际需求添加字段
    }
}
