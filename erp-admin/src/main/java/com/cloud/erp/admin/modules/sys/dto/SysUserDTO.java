package com.cloud.erp.admin.modules.sys.dto;

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
public class SysUserDTO  implements Serializable {


    @NotBlank(message = "账号不能为空")
    private String adminAccount;

    @NotBlank(message = "密码不能为空")
    private String adminAccountPassword;

    private  String adminSalt;
}
