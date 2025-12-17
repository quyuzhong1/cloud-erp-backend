package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 费用管理配置表
 * </p>
 *
 * @author will
 * @since 2024-03-15
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_cfg_cost")
public class TmsCfgCostEntity extends BaseEntity<TmsCfgCostEntity> {

    /**
    * 费用归属（字典dictCostAttribution）
    */
    @TableField("dict_cost_attribution")
    private String dictCostAttribution;

    /**
    * 费用分类（字典dictCostCategory）
    */
    @TableField("dict_cost_category")
    private String dictCostCategory;

    /**
    * 费用名称
    */
    @TableField("cost_name")
    private String costName;

    /**
     * 是否默认
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 是否分摊（t是，f否）
     */
    @TableField("is_allocate")
    private Boolean isAllocate;


    public static final String DICT_COST_ATTRIBUTION = "dict_cost_attribution";

    public static final String DICT_COST_CATEGORY = "dict_cost_category";

    public static final String COST_NAME = "cost_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}