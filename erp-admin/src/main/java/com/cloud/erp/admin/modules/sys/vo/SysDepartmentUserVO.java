package com.cloud.erp.admin.modules.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname SysDepartmentUserVO
 * @Description TODO
 * @Date 2022-07-19 12:03
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysDepartmentUserVO  extends SysUserVO{


    //领导状态 0 不是  1 是
    private Integer leadState;
}
