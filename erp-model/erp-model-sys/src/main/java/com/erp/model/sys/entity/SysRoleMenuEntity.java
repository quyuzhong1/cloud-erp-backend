package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;

/**
 * ${comments}
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role_menu")
public class SysRoleMenuEntity extends BaseEntity<SysRoleMenuEntity> {

	/**
	 * 菜单id
	 */
	private String menuId;
	/**
	 * 角色id
	 */
	private String roleId;

	/**
	 * 数据权限(1-自己,2-部门,3-全部)
	 */
	private Integer dataScope;

}
