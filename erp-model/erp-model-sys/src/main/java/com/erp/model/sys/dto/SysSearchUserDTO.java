package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname SysSearchUserDTO
 * @Description TODO
 * @Date 2022-07-13 10:49
 * @Created by yl
 */
@NoArgsConstructor
@Data
public class SysSearchUserDTO  implements Serializable {

    //类型 1：角色的 2:岗位 3:部门
    @NotNull(message = "菜单属性不能为空")
    @Range(min = 1, max = 3, message = "类型错误")
    private Integer sysType;

    //标示id
    private String flagId;

    private String searchKeyWord ;


}
