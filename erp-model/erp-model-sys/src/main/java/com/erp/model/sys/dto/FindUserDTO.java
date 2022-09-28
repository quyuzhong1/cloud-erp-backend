package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname SysUserDTO
 * @Description TODO
 * @Date 2022-09-28 12:19
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FindUserDTO implements Serializable {
    private String userId;


    private String userName;

    private Integer isMyState;
}
