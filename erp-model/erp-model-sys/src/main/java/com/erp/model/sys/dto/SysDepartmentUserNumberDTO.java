package com.erp.model.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname SysDepartmentUserNumber
 * @Description TODO
 * @Date 2022-07-18 14:09
 * @Created by yl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysDepartmentUserNumberDTO implements Serializable {


    private String departmentId;

    private String departmentName;

    private String userId;

    private String userName;

    private String code;


}
