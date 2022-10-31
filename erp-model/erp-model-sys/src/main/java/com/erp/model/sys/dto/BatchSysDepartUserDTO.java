package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Set;

/**
 * @Classname SysRoleMenuBatchDTO
 * @Description TODO
 * @Date 2022-07-20 9:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BatchSysDepartUserDTO implements Serializable {

     //部门id
    @NotBlank(message = "部门id 不能为空")
    private String departmentId;

    //用户id集合
    @NotEmpty(message = "用户id集合 不能为空")
    private Set<String> userIds;
}
