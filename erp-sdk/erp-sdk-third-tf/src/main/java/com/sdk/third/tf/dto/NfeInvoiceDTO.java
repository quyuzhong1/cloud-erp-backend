package com.sdk.third.tf.dto;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 上传记录请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
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
        private NfeClienteDTO  cliente;

        /**
         * 产品信息
         */
        private NfeItensDTO  itens;
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
        private String quantity;
        /**
         * 销售单价
         */
        @Alias("unit_price")
        private BigDecimal unitPrice;
    }
}