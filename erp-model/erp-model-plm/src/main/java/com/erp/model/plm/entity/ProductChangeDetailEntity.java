package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;


/**
 * <p>
 * 产品变更信息表
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("product_change_detail")
public class ProductChangeDetailEntity extends BaseEntity<ProductChangeDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 变更字段
    */
    @TableField("field")
    private String field;
    /**
    * 变更原值
    */
    @TableField("old_value")
    private String oldValue;
    /**
    * 变更新值
    */
    @TableField("new_value")
    private String newValue;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 分组信息
    */
    @TableField("group_name")
    private String groupName;


    public static final String MAIN_ID = "main_id";

    public static final String FIELD = "field";

    public static final String OLD_VALUE = "old_value";

    public static final String NEW_VALUE = "new_value";

    public static final String REMARK = "remark";

    public static final String GROUP = "group";

    @Override
    public Serializable pkVal() {
        return null;
    }

}