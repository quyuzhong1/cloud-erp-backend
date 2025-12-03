package com.erp.model.oms.entity;

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
 * kol类型表
 * </p>
 *
 * @author will
 * @since 2025-12-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_kol_option")
public class CfgKolOptionEntity extends BaseEntity<CfgKolOptionEntity> {

    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 类型  cooperationType: 合作类型  , partnerType: 达人类型
    */
    @TableField("type")
    private String type;


    public static final String NAME = "name";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}