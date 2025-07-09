package com.sdk.third.tf.dto;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * nef生成发票
 * @author will
 * @date 2025/4/11 12:09
 */
@Data
@NoArgsConstructor
public class NfeInvoiceDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class NfeCreateDTO {
        /**
         * 开发者邮箱,默认gray@ulanzi.cn
         */
        @Alias("email_dev")
        private String emailDev;
        /**
         * excluir_clientes_produtos_pos_emissao
         */
        @Alias("excluir_clientes_produtos_pos_emissao")
        private Boolean excluirClientesProdutosPosEmissao = false;
        /**
         *是否使用透明API，默认true
         */
        @Alias("is_transparente")
        private Boolean isTransparente = true;
        /**
         * token，创建开票公司后返回的公司token信息
         */
        @Alias("token_empresa")
        private String tokenEmpresa;
        /**
         * WJKJ
         */
        @Alias("name_dev")
        private String nameDev = "WJKJ";
        /**
         * 客户信息
         */
        @Alias("cliente")
        private NfeClienteDTO  cliente;

        /**
         * 产品信息
         */
        @Alias("itens")
        private List<NfeItensDTO> itens;

        /**
         * 付款信息
         */
        @Alias("payment")
        private List<NfePayMentDTO> payment;
        /**
         * 总金额，unit_price*销售数量，多行明细汇总；unit_price计算见明细
         */
        @Alias("valor_total")
        private BigDecimal valorTotal;
        /**
         * 最终申报价
         *
         * 配置了含运费开票：valor_total+运费
         *
         * 未配置含运费开票：默认等于valor_total
         */
        @Alias("final_total")
        private BigDecimal finalTotal;
    }

    @Data
    @NoArgsConstructor
    public static class NfePayMentDTO {
        /**
         * 金额
         */
        @Alias("amount")
        private BigDecimal amount;
        /**
         * 付款方式
         */
        @Alias("method")
        private String method;
        /**
         * 卡类型
         */
        @Alias("card_type")
        private String cardType;

        /**
         * 时间（当天）
         */
        @Alias("vencimento")
        private String vencimento;

        /**
         * 记录
         */
        @Alias("note")
        private String note;
    }

    @Data
    @NoArgsConstructor
    public static class NfeClienteDTO {
        /**
         * 区
         */
        @Alias("bairro")
        private String bairro;
        /**
         * 邮编
         */
        @Alias("cep")
        private String cep;
        /**
         * 城市编码
         */
        @Alias("city_id")
        private String cityId;
        /**
         * 国家
         */
        @Alias("country")
        private String country;
        /**
         * CNPJ
         */
        @Alias("cpf_cnpj")
        private String cpfCnpj;
        /**
         * 邮箱
         */
        @Alias("email")
        private String email;
        /**
         * 电话
         */
        @Alias("mobile")
        private String mobile;
        /**
         * 姓名
         */
        @Alias("name")
        private String name;
        /**
         * 门牌号
         */
        @Alias("numero")
        private String numero;
        /**
         * 地址
         */
        @Alias("rua")
        private String rua;
        /**
         * 州
         */
        @Alias("state")
        private String state;
        /**
         * 注册号
         */
        @Alias("ie_rg")
        private String ieRg;

        /**
         * 州（省份）二字码缩写
         */
        @Alias("uf")
        private String uf;
    }

    @Data
    @NoArgsConstructor
    public static class NfeItensDTO {
        /**
         * 跨州销售CFOP
         */
        @Alias("cfop_externo")
        private String cfopExterno;
        /**
         * 州内销售CFOP
         */
        @Alias("cfop_interno")
        private String cfopInterno;
        /**
         * 平台SKU
         */
        @Alias("sku")
        private String sku;
        /**
         * 产品开票名称
         */
        @Alias("name")
        private String name;
        /**
         * 商品ncm
         */
        @Alias("ncm")
        private String ncm;
        /**
         * 销售数量
         */
        @Alias("quantity")
        private Integer quantity;
        /**
         * 销售单价
         */
        @Alias("unit_price")
        private BigDecimal unitPrice;

        /**
         * 平台订单号
         */
        @Alias("co_ped_cliente_api")
        private String coPedClienteApi;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NfeCancelDTO {

        /**
         * 关系id
         */
        @Alias("transaction_id")
        private String transactionId;

        /**
         * 原因
         */
        @Alias("justificativa")
        private String justificativa;

        /**
         * token
         */
        @Alias("token_empresa")
        private String tokenEmpresa;

        /**
         * 是否向客户端发送消息
         */
        @Alias("enviar_email_para_cliente")
        private Boolean enviarEmailParaCliente = true;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NfeReturnDTO {

        /**
         * 关系id
         */
        @Alias("chave_nfe")
        private String chaveNfe;

        /**
         * 原因
         */
        @Alias("motivo")
        private String motivo;

        /**
         * token
         */
        @Alias("token_empresa")
        private String tokenEmpresa;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NfeListParamDTO {

        /**
         * 关系id
         */
        @Alias("transaction_id")
        private String transactionId;

        /**
         * token
         */
        @Alias("token_empresa")
        private String tokenEmpresa;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NfeCceDTO {
        /**
         * id
         */
        @Alias("id")
        private String id;
        /**
         * 原因
         */
        @Alias("justificativa")
        private String justificativa;
        /**
         * token
         */
        @Alias("token_empresa")
        private String tokenEmpresa;
        /**
         * nameDev
         */
        @Alias("name_dev")
        private String nameDev;
        /**
         * emailDev
         */
        @Alias("email_dev")
        private String emailDev;
    }


    @Data
    @NoArgsConstructor
    public static class NfeSuccessResultDTO {
        /**
         * 是否成功
         */
        @Alias("successo")
        private Boolean successo;
        /**
         * recibo
         */
        @Alias("recibo")
        private String recibo;
        /**
         * 状态,200成功
         */
        @Alias("status")
        private Integer status;
        /**
         * id
         */
        @Alias("id")
        private String id;
        /**
         * link_nota,pdf文件
         */
        @Alias("xml")
        private String xml;
        /**
         * link_nota,pdf文件
         */
        @Alias("link_nota")
        private String link_nota;
        /**
         * link_xml,xml文件
         */
        @Alias("link_xml")
        private String link_xml;
        /**
         * 序列号
         */
        @Alias("serie")
        private Integer serie;
        /**
         * 起始编码
         */
        @Alias("numero_nfe")
        private Integer numeroNfe;
    }

    @Data
    @NoArgsConstructor
    public static class NfeCceResultDTO {
        /**
         * 是否错误
         */
        @Alias("erro")
        private Boolean erro;
        /**
         * 状态
         */
        @Alias("status")
        private Integer status;
        /**
         * xml第三方本地路径
         */
        @Alias("xml_path")
        private String xmlPath;
        /**
         * xml可下载路径
         */
        @Alias("url_xml_upload")
        private String urlXmlUpload;
    }
}