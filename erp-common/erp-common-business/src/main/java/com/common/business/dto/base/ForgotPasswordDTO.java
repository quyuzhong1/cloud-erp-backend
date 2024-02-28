package com.common.business.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ForgotPasswordDTO implements Serializable {
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 验证码
     */
    private String verificationCode;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户类型
     */
    @JsonIgnore
    private String userType;
}
