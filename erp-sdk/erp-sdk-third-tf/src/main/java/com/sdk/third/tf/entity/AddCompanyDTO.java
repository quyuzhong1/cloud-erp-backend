package com.sdk.third.tf.entity;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Data
public class AddCompanyDTO extends CompanyDTO{

    @Alias("city")
    private String city;

}
