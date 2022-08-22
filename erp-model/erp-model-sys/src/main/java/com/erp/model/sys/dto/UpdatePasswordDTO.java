package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname UpdatePasswordDTO
 * @Description TODO
 * @Date 2022-07-18 11:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdatePasswordDTO implements Serializable {


    @NotBlank(message = "原始密码不能为空")
    private String oldPassword;


    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

}
