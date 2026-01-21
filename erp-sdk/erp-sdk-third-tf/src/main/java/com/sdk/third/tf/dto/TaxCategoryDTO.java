package com.sdk.third.tf.dto;

import cn.hutool.core.annotation.Alias;
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
        @Alias("descricao")
        private String descricao;
        
        @Alias("icms")
        private List<IcmsDetailDTO> icms;
        
        @Alias("ipi")
        private List<IpiDetailDTO> ipi;
        
        @Alias("pis")
        private List<PisDetailDTO> pis;
        
        @Alias("cofins")
        private List<CofinsDetailDTO> cofins;
    }

    /**
     * 编辑税种请求DTO
     */
    @Data
    @NoArgsConstructor
    public static class EditCategoryDTO implements Serializable {
        @Alias("category_id")
        private String categoryId;
        
        @Alias("descricao")
        private String descricao;
        
        @Alias("icms")
        private List<IcmsDetailDTO> icms;
        
        @Alias("ipi")
        private List<IpiDetailDTO> ipi;
        
        @Alias("pis")
        private List<PisDetailDTO> pis;
        
        @Alias("cofins")
        private List<CofinsDetailDTO> cofins;
    }

    /**
     * 删除税种请求DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeleteCategoryDTO implements Serializable {
        @Alias("category_id")
        private String categoryId;
    }

    /**
     * ICMS详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class IcmsDetailDTO implements Serializable {
        @Alias("tipo_tributacao")
        private String tipoTributacao;
        
        @Alias("cenario")
        private String cenario;
        
        @Alias("tipo_pessoa")
        private String tipoPessoa;
        
        @Alias("nao_contribuinte")
        private Boolean naoContribuinte;
        
        @Alias("codigo_cfop")
        private String codigoCfop;
        
        @Alias("situacao_tributaria")
        private String situacaoTributaria;
        
        @Alias("aliquota_importacao")
        private String aliquotaImportacao;
        
        @Alias("aliquota_credito")
        private String aliquotaCredito;
    }

    /**
     * IPI详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class IpiDetailDTO implements Serializable {
        @Alias("cenario")
        private String cenario;
        
        @Alias("tipo_pessoa")
        private String tipoPessoa;
        
        @Alias("situacao_tributaria")
        private String situacaoTributaria;
        
        @Alias("codigo_enquadramento")
        private String codigoEnquadramento;
        
        @Alias("aliquota")
        private String aliquota;
    }

    /**
     * PIS详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class PisDetailDTO implements Serializable {
        @Alias("cenario")
        private String cenario;
        
        @Alias("tipo_pessoa")
        private String tipoPessoa;
        
        @Alias("situacao_tributaria")
        private String situacaoTributaria;
        
        @Alias("aliquota")
        private String aliquota;
    }

    /**
     * COFINS详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class CofinsDetailDTO implements Serializable {
        @Alias("cenario")
        private String cenario;
        
        @Alias("tipo_pessoa")
        private String tipoPessoa;
        
        @Alias("situacao_tributaria")
        private String situacaoTributaria;
        
        @Alias("aliquota")
        private String aliquota;
    }

    /**
     * 税种列表项DTO
     */
    @Data
    @NoArgsConstructor
    public static class CategoryListItemDTO implements Serializable {
        @Alias("category_id")
        private String categoryId;
        
        @Alias("descricao")
        private String descricao;
    }

    /**
     * 税种详情响应DTO
     */
    @Data
    @NoArgsConstructor
    public static class CategoryDetailDTO implements Serializable {
        @Alias("category_id")
        private String categoryId;
        
        @Alias("descricao")
        private String descricao;
        
        @Alias("icms")
        private List<IcmsDetailDTO> icms;
        
        @Alias("ipi")
        private List<IpiDetailDTO> ipi;
        
        @Alias("pis")
        private List<PisDetailDTO> pis;
        
        @Alias("cofins")
        private List<CofinsDetailDTO> cofins;
    }

    /**
     * 税种列表响应DTO
     */
    @Data
    @NoArgsConstructor
    public static class CategoryListResponseDTO implements Serializable {
        @Alias("list")
        private List<CategoryListItemDTO> list;
        
        @Alias("page")
        private Integer page;
        
        @Alias("total")
        private Integer total;
        
        @Alias("total_pages")
        private Integer totalPages;
    }

    /**
     * 创建税种响应DTO
     */
    @Data
    @NoArgsConstructor
    public static class CreateCategoryResponseDTO implements Serializable {
        @Alias("category_id")
        private String categoryId;
    }

    /**
     * 编辑税种响应DTO
     */
    @Data
    @NoArgsConstructor
    public static class EditCategoryResponseDTO implements Serializable {
        @Alias("category_id")
        private String categoryId;
    }

}
