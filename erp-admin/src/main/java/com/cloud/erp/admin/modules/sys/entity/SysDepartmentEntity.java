package com.cloud.erp.admin.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 部门表
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@Data
@TableName("sys_department")
public class SysDepartmentEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * $column.comments
	 */
	@TableId(type = IdType.INPUT)
	private String id;
	/**
	 * $column.comments
	 */
	private String name;

	/**
	 * 备注
	 */
	private String remark;

	//1 部门  2 小组
	private Integer type;
	/**
	 * 创建时间
	 */
	@TableField(fill= FieldFill.INSERT)
	private Date createTime;
	/**
	 * 更新时间
	 */
	@TableField(fill= FieldFill.INSERT_UPDATE)
	private Date updateTime;
	/**
	 * 父级id
	 */
	private String parentId;

}
