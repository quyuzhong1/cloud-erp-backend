package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SysUserDeptDTO {
    /**
     * 用户id
     */
    private String uid;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 部门id
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;


    /**
     * 用户状态1：正常 0：禁用
     */
    private Integer userState;

}
