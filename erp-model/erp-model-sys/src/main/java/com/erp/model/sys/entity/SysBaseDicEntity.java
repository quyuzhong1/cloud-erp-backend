package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author yl
 * @since 2022-08-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_base_dic")
public class SysBaseDicEntity extends BaseEntity<SysBaseDicEntity> {

    /**
     * 字典属性
     */
    @TableField("dic_type")
    private String dicType;

    /**
     * 对应的值
     */
    @TableField("dic_value")
    private String dicValue;

    /**
     * 标题
     */
    @TableField("dic_title")
    private String dicTitle;


}
