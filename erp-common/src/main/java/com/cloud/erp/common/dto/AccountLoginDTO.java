package com.cloud.erp.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname 账号登录 入参
 * @Description TODO
 * @Date 2022-07-08 16:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class AccountLoginDTO implements Serializable {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;
}
