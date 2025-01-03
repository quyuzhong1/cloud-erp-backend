package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 分区表
 * </p>
 *
 * @author lrp
 * @since 2025-01-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_partition")
public class DictPartitionEntity extends BaseEntity<DictPartitionEntity> {

    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 编码
    */
    @TableField("code")
    private String code;
    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 排序字段
    */
    @TableField("index")
    private Integer index;


    public static final String DISABLED = "disabled";

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}