package com.sdk.third.tf.entity;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Data
public class UpdateCompanyDTO extends CompanyDTO{

    @Alias("id")
    private String id;

    @Alias("city_id")
    private String city;

}
