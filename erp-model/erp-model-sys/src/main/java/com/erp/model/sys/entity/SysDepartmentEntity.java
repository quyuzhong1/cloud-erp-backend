package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;

/**
 * 部门表
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_department")
public class SysDepartmentEntity extends BaseEntity<SysDepartmentEntity> {

	/**
	 * $column.comments
	 */
	@TableId(type = IdType.INPUT)
	private String id;

	/**
	 * 编码
	 */
	@TableField("code")
	private String code;

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
	 * 父级id
	 */
	private String parentId;

	/**
	 * 同步金蝶id
	 */
	@TableField("sync_kingdee_id")
	private String syncKingdeeId;

	/**
	 * 所有上级IDS
	 */
	@TableField(exist = false)
	private String path = "";

	/**
	 * 是否禁用
	 */
	@TableField("disabled")
	private Boolean disabled;

}
