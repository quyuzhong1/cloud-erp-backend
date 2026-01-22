package com.erp.model.sys.vo;

import lombok.*;

/**
 * 部门下拉列表
 *
 * @Author Cloud
 * @Date 2022/12/19 9:44
 **/
@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SysDeptDropDownVO {

    /**
     * 部门Id
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 是否禁用 ： true 禁用  false 启用
     */
    private Boolean disabled;

}
