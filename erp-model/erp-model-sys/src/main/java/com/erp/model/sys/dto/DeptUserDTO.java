package com.erp.model.sys.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname DeptUserDTO

 * @Date 2023-01-06 17:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DeptUserDTO implements Serializable {

    private String id;
    /**
     * $column.comments
     */
    private String name;

    //类型 1 部门  2 小组
    private Integer type;

    //用户人数
    private Integer userNumber;


    private String parentId;

    private String parentName;

    private List<SysDepartmentUserNumberDTO> userList;


    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private List<DeptUserDTO> childrenList;
}
