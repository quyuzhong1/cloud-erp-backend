package com.erp.model.dmp.entity;

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
 * 差异策略配置基础信息
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_diff_strategy")
public class CfgDiffStrategyEntity extends BaseEntity<CfgDiffStrategyEntity> {

    /**
    * 配置编码
    */
    @TableField("code")
    private String code;
    /**
    * 配置名称
    */
    @TableField("name")
    private String name;
    /**
    * 单据类型
    */
    @TableField("bill_type")
    private String billType;
    /**
    * 执行状态
    */
    @TableField("status")
    private Boolean status;


    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String BILL_TYPE = "bill_type";

    public static final String STATUS = "status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}