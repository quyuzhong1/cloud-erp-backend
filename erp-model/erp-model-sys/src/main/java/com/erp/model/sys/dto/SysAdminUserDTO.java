package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname SysUserDTO
 * @Description TODO
 * @Date 2022-07-01 16:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
@ToString
public class SysAdminUserDTO implements Serializable {


    @NotBlank(message = "账号不能为空")
    private String loginAccount;

    @NotBlank(message = "密码不能为空")
    private String password;


    private String userName;


}
