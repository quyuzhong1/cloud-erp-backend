package com.sdk.third.tf.entity;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CompanyDTO {

    @Alias("ultimo_numero_nfe")
    private String ultimoNumeroNfe;
    @Alias("token_empresa")
    private String tokenEmpresa;
    @Alias("token_plataforma")
    private String tokenPlataforma;
    @Alias("telefone")
    private String telefone;
    @Alias("is_active")
    private Integer isActive;
    @Alias("numero")
    private String numero;
    @Alias("bairro")
    private String bairro;
    @Alias("certificado_via_link")
    private Boolean certificadoViaLink;
    @Alias("senha_certificado")
    private String senhaCertificado;
    @Alias("cnpj")
    private String cnpj;
    @Alias("zip_code")
    private String zipCode;
    @Alias("certificado")
    private String certificado;
    @Alias("cep")
    private String cep;
    @Alias("password")
    private String password;
    @Alias("name")
    private String name;
    @Alias("razao_social")
    private String razaoSocial;
    @Alias("numero_serie_nfe")
    private String numeroSerieNfe;
    @Alias("state")
    private String state;
    @Alias("ie")
    private String ie;
    @Alias("api_completa")
    private Boolean apiCompleta;
    @Alias("first_name")
    private String firstName;
    @Alias("email")
    private String email;
    @Alias("username")
    private String username;
    @Alias("rua")
    private String rua;

    @Alias("natureza_id")
    private String naturezaId;

    @Alias("surname")
    private String surname;

    @Alias("last_name")
    private String lastName;

    @Alias("landmark")
    private String landmark;

    @Alias("ambiente")
    private String ambiente;

    private String city;

}
