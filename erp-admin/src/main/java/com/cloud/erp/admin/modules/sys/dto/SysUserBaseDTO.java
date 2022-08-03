package com.cloud.erp.admin.modules.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @Classname SysUserBaseDTO
 * @Description TODO
 * @Date 2022-08-02 14:47
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserBaseDTO implements Serializable {

    @Size(max = 10,message = "真实名最大长度为10")
    private String realName;
}
