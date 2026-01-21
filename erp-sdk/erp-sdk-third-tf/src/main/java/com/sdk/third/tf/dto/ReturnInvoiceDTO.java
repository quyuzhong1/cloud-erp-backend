package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 退货发票请求DTO（新接口）
 * 对应新接口路径：/api/invoice/return
 * 
 * @author system
 * @date 2025/01/XX
 */
@Data
public class ReturnInvoiceDTO implements Serializable {

    /**
     * 发票id/chave（第三方生成的发票，开具请传chave）
     * 注意：JSON字段名为"uuid/chave"，但Java属性名不能包含斜杠，所以使用uuidOrChave
     */
    @NotBlank(message = "发票uuid/chave不能为空")
    @JsonProperty("uuid/chave")
    private String uuidOrChave;

    /**
     * 退货CFOP（4位数字）
     */
    @NotBlank(message = "退货CFOP不能为空")
    @JsonProperty("cfop")
    private String cfop;

    /**
     * 退货原因（natureza_operacao）
     */
    @NotBlank(message = "退货原因不能为空")
    @JsonProperty("natureza_operacao")
    private String naturezaOperacao;

    /**
     * 退货详情（第三方生成的发票需填写）
     */
    @JsonProperty("return_detail")
    private ReturnDetailDTO returnDetail;

    /**
     * 退货详情DTO
     */
    @Data
    public static class ReturnDetailDTO implements Serializable {
        /**
         * 发票ID（建议使用订单ID，不超过15字符）
         */
        @NotBlank(message = "发票ID不能为空")
        @JsonProperty("id")
        private String id;

        /**
         * 交易类型（例如：1-出项发票）
         */
        @NotBlank(message = "交易类型不能为空")
        @JsonProperty("transaction_type")
        private String transactionType;

        /**
         * 发票模式（例如：55-NFe）
         */
        @NotBlank(message = "发票模式不能为空")
        @JsonProperty("model")
        private String model;

        /**
         * 发票类型（例如：1-正常）
         */
        @NotBlank(message = "发票类型不能为空")
        @JsonProperty("issuance_type")
        private String issuanceType;

        /**
         * 运行环境（1-生产，2-测试）
         */
        @NotBlank(message = "运行环境不能为空")
        @JsonProperty("ambiente")
        private String ambiente;

        /**
         * 总折扣金额（包含在发票总金额中）
         */
        @JsonProperty("total_discount_amount")
        private java.math.BigDecimal totalDiscountAmount;

        /**
         * 客户信息
         */
        @NotNull(message = "客户信息不能为空")
        @Valid
        @JsonProperty("cliente")
        private CreateInvoiceDTO.ClienteDTO cliente;

        /**
         * 商品列表
         */
        @NotNull(message = "商品列表不能为空")
        @Valid
        @JsonProperty("products")
        private java.util.List<CreateInvoiceDTO.ProductDTO> products;

        /**
         * 运输信息
         */
        @NotNull(message = "运输信息不能为空")
        @Valid
        @JsonProperty("transportation")
        private CreateInvoiceDTO.TransportationDTO transportation;

        /**
         * 发货地址（可选）
         */
        @JsonProperty("shipping_address")
        private CreateInvoiceDTO.AddressDTO shippingAddress;

        /**
         * 收货地址（可选）
         */
        @JsonProperty("delivery_address")
        private CreateInvoiceDTO.AddressDTO deliveryAddress;
    }
}
