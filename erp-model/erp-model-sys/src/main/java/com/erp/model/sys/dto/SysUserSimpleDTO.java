package com.erp.model.sys.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Classname: SysUserSimpleDTO
 * @Description: TODO
 * @CreateTime: 2023-04-23  09:55
 * @Author: zhangchunlin
 */
@Data
public class SysUserSimpleDTO implements Serializable {

    private String uid;

    private String userName;

    private String realName;

    private String mobile;

    private String email;



}