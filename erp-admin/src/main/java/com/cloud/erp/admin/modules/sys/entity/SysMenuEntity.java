package com.cloud.erp.admin.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 菜单表
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@Data
@TableName("sys_menu")
public class SysMenuEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * $column.comments
	 */
	@TableId(type = IdType.INPUT)
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
	 * 菜单链接
	 */
	private String menuUrl;
	/**
	 * 类型 1：目录   2：菜单   3：按钮  4：功能
	 */
	private Integer type;




}
