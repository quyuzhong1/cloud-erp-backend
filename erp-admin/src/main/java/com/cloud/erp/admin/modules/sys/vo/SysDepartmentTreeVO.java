package com.cloud.erp.admin.modules.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** 从数据库查询部门树结构
 * @Classname SysDepartmentTreeVO
 * @Description TODO
 * @Date 2022-08-01 14:04
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysDepartmentTreeVO implements Serializable {

    private String id;

    private String name;

    private String  parentId;

    private String  path;
}
