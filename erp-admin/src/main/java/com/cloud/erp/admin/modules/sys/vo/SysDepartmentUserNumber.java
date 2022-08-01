package com.cloud.erp.admin.modules.sys.vo;

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
public class SysDepartmentUserNumber implements Serializable {


    private String  departmentId;

    private String userId;


}
