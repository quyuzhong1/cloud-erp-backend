package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 系统配置管理
 * </p>
 *
 * @author Lambda
 * @since 2024-09-03
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_setting")
@EqualsAndHashCode(callSuper = true)
public class CfgSettingEntity extends BaseEntity<CfgSettingEntity> {

    private static final long serialVersionUID = -4162656980460609031L;
    /**
     * Key值
     */
    @TableField("key")
    private String key;

    /**
     * json数据
     */
    @TableField("data_json")
    private String dataJson;

    /**
     * 是否禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 排序字段
     */
    @TableField("index")
    private Integer index;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String KEY = "key";

    public static final String DATA_JSON = "data_json";

    public static final String DISABLED = "disabled";

    public static final String INDEX = "index";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
