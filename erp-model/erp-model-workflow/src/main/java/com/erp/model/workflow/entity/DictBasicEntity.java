package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 字典表
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_basic")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DictBasicEntity extends BaseEntity<DictBasicEntity> {

    /**
     * 备注 需要的时候 用到
     */
    @TableField("remark")
    private String remark;

    /**
     * value 使用值
     */
    @TableField("value")
    private String value;

    /**
     * type 分组
     */
    @TableField("type")
    private String type;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 启用状态
     */
    @TableField("status")
    private Boolean status;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;


    public static final String FIELD_REMARK = "remark";

    public static final String FIELD_VALUE = "value";

    public static final String FIELD_TYPE = "type";

    public static final String FIELD_NAME = "name";

    public static final String FIELD_STATUS = "status";

    public static final String FIELD_SORT = "sort";

}
