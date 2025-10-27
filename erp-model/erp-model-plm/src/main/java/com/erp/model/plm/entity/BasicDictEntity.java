package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * plm 字典表
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("basic_dict")
public class BasicDictEntity extends BaseEntity<BasicDictEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典值
     */
    @TableField("value")
    private String value;

    /**
     * 字典类型 productProperty 产品属性	productGrade 产品等级
     */
    @TableField("type")
    private String type;

    /**
     * 字段标签
     */
    @TableField("name")
    private String name;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 排序
     */
    @TableField("order_index")
    private Integer orderIndex;

    /**
     * 启用状态
     */
    @TableField("status")
    private Boolean status;
}
