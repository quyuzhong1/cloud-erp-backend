package com.erp.model.scm.entity;

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
 * 供应商资质字典表
 * </p>
 *
 * @author jack
 * @since 2025-10-15
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_credential")
public class DictCredentialEntity extends BaseEntity<DictCredentialEntity> {

    /**
    * 资质编号
    */
    @TableField("code")
    private String code;
    /**
    * 资质名称
    */
    @TableField("name")
    private String name;
    /**
    * 是否启用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 排序
    */
    @TableField("sort")
    private Integer sort;


    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String DISABLED = "disabled";

    public static final String SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }

}