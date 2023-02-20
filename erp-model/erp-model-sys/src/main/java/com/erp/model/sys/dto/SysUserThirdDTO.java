package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname SysUserBindingThirdDTO
 * @Description TODO
 * @Date 2022-07-13 17:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserThirdDTO implements Serializable {


    private String code;


    //FS,DD,QYWX
    private String bindingPlatform;




}
