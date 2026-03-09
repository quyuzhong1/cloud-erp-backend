package com.erp.model.sys.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Classname SysDepartmentVO

 * @Date 2022-07-11 15:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DepartmentDTO {


    private String id;
    /**
     * $column.comments
     */
    private String name;

    //类型 1 部门  2 小组
    private Integer type;

    //用户人数
    private Integer userNumber;

    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


    private String parentId;

    private String parentName;

    private Boolean disabled;


    @JsonInclude(value= JsonInclude.Include.NON_NULL)
    private List<DepartmentDTO> childrenList;
}
