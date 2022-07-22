package com.cloud.erp.admin.modules.sys.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @Classname SysDepartmentVO
 * @Description TODO
 * @Date 2022-07-11 15:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysDepartmentVO {


    private  String id;
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
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;


    private String parentId;

    private String parentName;


    @JsonInclude(value= JsonInclude.Include.NON_NULL)
    private List<SysDepartmentVO> childrenList;
}
