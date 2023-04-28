package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/14 10:34
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UserSuperiorDTO implements Serializable {

    /**
     * 当前用户id
     */
    private String currentUserId;

    /**
     * 上级用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 上级类型
     */
    private String superiorType;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门id
     */
    private String deptId;

    /**
     * 上级级别
     */
    private Integer level;


}
