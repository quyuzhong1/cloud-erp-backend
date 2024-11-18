package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname 账号登录 入参

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
    /**
     * erp ERP系统传参
     * srm SRM系统传参
     * pda PDA系统传参
     */
    @NotBlank(message = "所属系统不能为空")
    private String userType;
}
