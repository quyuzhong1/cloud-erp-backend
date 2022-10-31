package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname DepartmentSearchDTO
 * @Description TODO
 * @Date 2022-07-19 12:31
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DepartmentSearchDTO  implements Serializable {

    private String searchKeyword;

    @NotBlank(message = "部门id不能为空")
    private String departmentId;

    //状态 1 正常  0 不正常
    @Range(min = 0, max = 1, message = "状态类型错误")
    private Integer state;

    @NotBlank(message = "探索类型不能为空")
    //   @Pattern(regexp = "^[mobile realName userName]$", message = "搜素类型有误")
    private String searchType;


}
