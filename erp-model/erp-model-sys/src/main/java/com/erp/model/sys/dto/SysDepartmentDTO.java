package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Classname SysDepartmentDTO
 * @Description TODO
 * @Date 2022-07-11 17:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysDepartmentDTO {


    private String id;
    /**
     * $column.comments
     */
    private String name;

    //1 部门  2  小组
    private Integer type;

    /**
     * 备注
     */
    private String remark;


    private List<SysDepartmentDTO> childrenList;


}
