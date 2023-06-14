package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 产品归属规则配置表
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_product_owner_rule")
public class CfgProductOwnerRuleEntity extends BaseEntity<CfgProductOwnerRuleEntity> {


    /**
    * 分类id
    */
    @TableField("category_id")
    private String categoryId;

    /**
    * 组织id
    */
    @TableField("org_id")
    private String orgId;

    /**
     * 组织名
     */
    @TableField("org_name")
    private String orgName;


    public static final String CATEGORY_ID = "category_id";

    public static final String ORG_ID = "org_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}