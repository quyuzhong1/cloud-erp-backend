package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;

/**
 * 菜单表
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_menu")
public class SysMenuEntity extends BaseEntity<SysMenuEntity> {

	/**
	 * $column.comments
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String menuId;
	/**
	 * 父id
	 */
	private String parentId;
	/**
	 * 菜单图标
	 */
	private String menuIcon;
	/**
	 * 菜单名称
	 */
	private String menuName;

	/**
	 * 菜单code 码
	 */
	private String menuCode;

	/**
	 * 菜单链接
	 */
	private String menuUrl;
	/**
	 * 类型 1：目录   2：菜单   3：按钮  4：功能
	 */
	private Integer type;

	private String selectLightId;
	/**
	 * 禁用
	 */
	private Boolean disabled;


	/**
	 * 菜单排序
	 */
	private Integer index;

	/**
	 * 所属系统,/api/sys/dictBasic/list?type=menuSystem
	 */
	private String system;
}
