package com.erp.model.sys.dto;

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
public class SysDepartmentUserDTO extends UserDTO{


    //领导状态 0 不是  1 是
    private Integer leadState;


    private String departmentName;
}
