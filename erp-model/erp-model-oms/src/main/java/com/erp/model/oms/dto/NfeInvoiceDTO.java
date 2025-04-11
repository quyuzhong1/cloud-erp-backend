package com.erp.model.oms.dto;

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
        private String emailDev;
        /**
         * excluir_clientes_produtos_pos_emissao
         */
        private Boolean excluirClientesProdutosPosEmissao = false;
        /**
         *是否使用透明API，默认true
         */
        private Boolean isTransparente = true;
        /**
         * token，创建开票公司后返回的公司token信息
         */
        private String tokenEmpresa;
        /**
         * WJKJ
         */
        private String nameDev;
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
        private BigDecimal valorTotal;
        /**
         * 最终申报价
         *
         * 配置了含运费开票：valor_total+运费
         *
         * 未配置含运费开票：默认等于valor_total
         */
        private BigDecimal finalTotal;
    }

    @Data
    @NoArgsConstructor
    public static class NfeClienteDTO {
        /**
         * 区
         */
        private String bairro;
        /**
         * 邮编
         */
        private String cep;
        /**
         * 城市编码
         */
        private String cityId;
        /**
         * 国家
         */
        private String country;
        /**
         * CNPJ
         */
        private String cpfCnpj;
        /**
         * 邮箱
         */
        private String email;
        /**
         * 电话
         */
        private String mobile;
        /**
         * 姓名
         */
        private String name;
        /**
         * 门牌号
         */
        private String numero;
        /**
         * 地址
         */
        private String rua;
        /**
         * 州
         */
        private String state;
        /**
         * 注册号
         */
        private String ieRg;
    }

    @Data
    @NoArgsConstructor
    public static class NfeItensDTO {
        /**
         * 跨州销售CFOP
         */
        private String cfopExterno;
        /**
         * 州内销售CFOP
         */
        private String cfopInterno;
        /**
         * 平台SKU
         */
        private String sku;
        /**
         * 产品开票名称
         */
        private String name;
        /**
         * 商品ncm
         */
        private String ncm;
        /**
         * 销售数量
         */
        private String quantity;
        /**
         * 销售单价
         */
        private BigDecimal unitPrice;
    }
}