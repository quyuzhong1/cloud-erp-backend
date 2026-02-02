package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 税种相关DTO
 * @author system
 * @date 2025/01/XX
 */
public class TaxCategoryDTO {

    /**
     * 创建税种请求DTO
     */
    @Data
    @NoArgsConstructor
    public static class CreateCategoryDTO implements Serializable {
        @JsonProperty("descricao")
        private String descricao;
        
        @JsonProperty("icms")
        private List<IcmsDetailDTO> icms;
        
        @JsonProperty("ipi")
        private List<IpiDetailDTO> ipi;
        
        @JsonProperty("pis")
        private List<PisDetailDTO> pis;
        
        @JsonProperty("cofins")
        private List<CofinsDetailDTO> cofins;
    }

    /**
     * 编辑税种请求DTO
     */
    @Data
    @NoArgsConstructor
    public static class EditCategoryDTO implements Serializable {
        @JsonProperty("category_id")
        private String categoryId;
        
        @JsonProperty("descricao")
        private String descricao;
        
        @JsonProperty("icms")
        private List<IcmsDetailDTO> icms;
        
        @JsonProperty("ipi")
        private List<IpiDetailDTO> ipi;
        
        @JsonProperty("pis")
        private List<PisDetailDTO> pis;
        
        @JsonProperty("cofins")
        private List<CofinsDetailDTO> cofins;
    }

    /**
     * 删除税种请求DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeleteCategoryDTO implements Serializable {
        @JsonProperty("category_id")
        private String categoryId;
    }

    /**
     * ICMS详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class IcmsDetailDTO implements Serializable {
        @JsonProperty("tipo_tributacao")
        private String tipoTributacao;
        
        @JsonProperty("cenario")
        private String cenario;
        
        @JsonProperty("tipo_pessoa")
        private String tipoPessoa;
        
        @JsonProperty("nao_contribuinte")
        private Boolean naoContribuinte;
        
        @JsonProperty("codigo_cfop")
        private String codigoCfop;
        
        @JsonProperty("situacao_tributaria")
        private String situacaoTributaria;
        
        @JsonProperty("aliquota_importacao")
        private String aliquotaImportacao;
        
        @JsonProperty("aliquota_credito")
        private String aliquotaCredito;
    }

    /**
     * IPI详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class IpiDetailDTO implements Serializable {
        @JsonProperty("cenario")
        private String cenario;
        
        @JsonProperty("tipo_pessoa")
        private String tipoPessoa;
        
        @JsonProperty("situacao_tributaria")
        private String situacaoTributaria;
        
        @JsonProperty("codigo_enquadramento")
        private String codigoEnquadramento;
        
        @JsonProperty("aliquota")
        private String aliquota;
    }

    /**
     * PIS详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class PisDetailDTO implements Serializable {
        @JsonProperty("cenario")
        private String cenario;
        
        @JsonProperty("tipo_pessoa")
        private String tipoPessoa;
        
        @JsonProperty("situacao_tributaria")
        private String situacaoTributaria;
        
        @JsonProperty("aliquota")
        private String aliquota;
    }

    /**
     * COFINS详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class CofinsDetailDTO implements Serializable {
        @JsonProperty("cenario")
        private String cenario;
        
        @JsonProperty("tipo_pessoa")
        private String tipoPessoa;
        
        @JsonProperty("situacao_tributaria")
        private String situacaoTributaria;
        
        @JsonProperty("aliquota")
        private String aliquota;
    }

    /**
     * 税种列表项DTO
     */
    @Data
    @NoArgsConstructor
    public static class CategoryListItemDTO implements Serializable {
        @JsonProperty("category_id")
        private String categoryId;
        
        @JsonProperty("descricao")
        private String descricao;
    }

    /**
     * 税种详情响应DTO
     */
    @Data
    @NoArgsConstructor
    public static class CategoryDetailDTO implements Serializable {
        @JsonProperty("category_id")
        private String categoryId;
        
        @JsonProperty("descricao")
        private String descricao;
        
        @JsonProperty("icms")
        private List<IcmsDetailDTO> icms;
        
        @JsonProperty("ipi")
        private List<IpiDetailDTO> ipi;
        
        @JsonProperty("pis")
        private List<PisDetailDTO> pis;
        
        @JsonProperty("cofins")
        private List<CofinsDetailDTO> cofins;
    }

    /**
     * 税种列表响应DTO
     */
    @Data
    @NoArgsConstructor
    public static class CategoryListResponseDTO implements Serializable {
        @JsonProperty("list")
        private List<CategoryListItemDTO> list;
        
        @JsonProperty("page")
        private Integer page;
        
        @JsonProperty("total")
        private Integer total;
        
        @JsonProperty("total_pages")
        private Integer totalPages;
    }

    /**
     * 创建税种响应DTO
     */
    @Data
    @NoArgsConstructor
    public static class CreateCategoryResponseDTO implements Serializable {
        @JsonProperty("category_id")
        private String categoryId;
    }

    /**
     * 编辑税种响应DTO
     */
    @Data
    @NoArgsConstructor
    public static class EditCategoryResponseDTO implements Serializable {
        @JsonProperty("category_id")
        private String categoryId;
    }

}
