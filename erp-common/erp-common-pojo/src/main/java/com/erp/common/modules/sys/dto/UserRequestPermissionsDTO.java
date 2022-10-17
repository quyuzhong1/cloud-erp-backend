package com.erp.common.modules.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户权限返回数据
 * @Classname
 * @Description TODO
 * @Date 2022-10-15 11:10
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UserRequestPermissionsDTO implements Serializable {


    /**
     * 权限的code
     */
    private String permissionsCode;

    /**
     * 数据权限的范围
     * 数据权限(1-自己,2-部门,3-全部
     */
    private Integer dataScope;
}
