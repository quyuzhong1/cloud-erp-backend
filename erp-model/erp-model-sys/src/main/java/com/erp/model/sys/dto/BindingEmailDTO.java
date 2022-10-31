package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname BindingEmailDTO
 * @Description TODO
 * @Date 2022-08-02 10:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BindingEmailDTO implements Serializable {


    private String email;

    private String emailCode;
}
