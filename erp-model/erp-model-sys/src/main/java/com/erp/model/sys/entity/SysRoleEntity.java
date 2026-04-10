package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;

/**
 * 角色表
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role")
public class SysRoleEntity extends BaseEntity<SysRoleEntity> {

	/**
	 * 角色名
	 */
	private String roleName;
	/**
	 * 角色描述
	 */
	private String roleRemark;

}
