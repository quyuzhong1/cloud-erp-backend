package com.sdk.third.tf.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class CompanyInfoEntity {


    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("username")
    private String username;
    @JsonProperty("email")
    private String email;
    @JsonProperty("telefone")
    private String telefone;
    @JsonProperty("is_active")
    private Integer isActive;
    @JsonProperty("razao_social")
    private String razaoSocial;
    @JsonProperty("cnpj")
    private String cnpj;
    @JsonProperty("ie")
    private String ie;
    @JsonProperty("senha_certificado")
    private String senhaCertificado;
    @JsonProperty("certificado")
    private String certificado;
    @JsonProperty("expericao_certificado")
    private String expericaoCertificado;
    @JsonProperty("busNomeFantasia")
    private String busNomeFantasia;
    @JsonProperty("zip_code")
    private String zipCode;
    @JsonProperty("state")
    private String state;
    @JsonProperty("city")
    private String city;
    @JsonProperty("bairro")
    private String bairro;
    @JsonProperty("rua")
    private String rua;
    @JsonProperty("numero")
    private String numero;
    @JsonProperty("cep")
    private String cep;
    @JsonProperty("ultimo_numero_nfe")
    private Integer ultimoNumeroNfe;
    @JsonProperty("ultimo_numero_nfce")
    private Integer ultimoNumeroNfce;
    @JsonProperty("ultimo_numero_cte")
    private Integer ultimoNumeroCte;
    @JsonProperty("ultimo_numero_mdfe")
    private Integer ultimoNumeroMdfe;
    @JsonProperty("inscricao_municipal")
    private String inscricaoMunicipal;
    @JsonProperty("numero_serie_nfe")
    private Integer numeroSerieNfe;
    @JsonProperty("numero_serie_nfce")
    private Integer numeroSerieNfce;
    @JsonProperty("numero_serie_cte")
    private String numeroSerieCte;
    @JsonProperty("numero_serie_mdfe")
    private String numeroSerieMdfe;
    @JsonProperty("cst_csosn_padrao")
    private Integer cstCsosnPadrao;
    @JsonProperty("cst_cofins_padrao")
    private Integer cstCofinsPadrao;
    @JsonProperty("cst_pis_padrao")
    private Integer cstPisPadrao;
    @JsonProperty("cst_ipi_padrao")
    private Integer cstIpiPadrao;
    @JsonProperty("token_plataforma")
    private TokenPlataformaDTO tokenPlataforma;
    @JsonProperty("token_empresa")
    private String tokenEmpresa;
    @JsonProperty("impostos")
    private List<ImpostosDTO> impostos;

    @NoArgsConstructor
    @Data
    public static class TokenPlataformaDTO {
        @JsonProperty("tk_id")
        private Integer tkId;
        @JsonProperty("tk_business_id")
        private Integer tkBusinessId;
        @JsonProperty("tk_valor")
        private String tkValor;
    }

    @NoArgsConstructor
    @Data
    public static class ImpostosDTO {
        @JsonProperty("natureza")
        private String natureza;
        @JsonProperty("cfop_entrada_estadual")
        private String cfopEntradaEstadual;
        @JsonProperty("cfop_entrada_inter_estadual")
        private String cfopEntradaInterEstadual;
        @JsonProperty("cfop_saida_estadual")
        private String cfopSaidaEstadual;
        @JsonProperty("cfop_saida_inter_estadual")
        private String cfopSaidaInterEstadual;
        @JsonProperty("sobrescreve_cfop")
        private Integer sobrescreveCfop;
        @JsonProperty("bonificacao")
        private Integer bonificacao;
        @JsonProperty("tipo")
        private Integer tipo;
        @JsonProperty("finNFe")
        private Integer finNFe;
        @JsonProperty("id")
        private Integer id;
    }
}
