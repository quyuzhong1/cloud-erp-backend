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
    private String userId;

    private String code;

    private String userName;

    private String realName;


    private String mobile;

    private Integer isMyState;

    private String departmentId;

    private String departmentName;
}
