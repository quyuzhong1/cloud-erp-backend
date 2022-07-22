package com.cloud.erp.admin.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色表
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@Data
@TableName("sys_role")
public class SysRoleEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * $column.comments
	 */
	@TableId(value = "id",type = IdType.ASSIGN_ID)
	private String id;
	/**
	 * 角色名
	 */
	private String roleName;
	/**
	 * 角色描述
	 */
	private String roleRemark;

	/**
	 * 创建时间
	 */
	@TableField(fill= FieldFill.INSERT)
	private Date createTime;
	/**
	 * 修改时间
	 */
	@TableField(fill= FieldFill.INSERT_UPDATE)
	private Date updateTime;

}
