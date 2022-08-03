package com.erp.common.modules.email.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname EmailVerifyCodeDTO
 * @Description TODO
 * @Date 2022-08-02 10:17
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class EmailVerifyCodeDTO implements Serializable {

    //邮箱
    private String email;

    private String verifyCode;

    private String date;



}
