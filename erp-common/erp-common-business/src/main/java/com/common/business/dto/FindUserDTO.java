package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname FindUserDTO
 * @Date 2022-10-08 11:22
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FindUserDTO implements Serializable {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 金蝶code
     */
    private String code;

    /**
     * 金蝶id
     */
    private String syncKingdeeId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 电话
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 是否自己
     */
    private Integer isMyState;
    /**
     * 部门id
     */
    private String departmentId;
    /**
     * 部门名
     */
    private String departmentName;
}
