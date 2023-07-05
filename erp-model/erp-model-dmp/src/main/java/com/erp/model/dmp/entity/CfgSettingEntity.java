package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.erp.model.dmp.enums.SettingEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 服务配置表
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_setting")
public class CfgSettingEntity extends BaseEntity<CfgSettingEntity> {


    /**
    * 配置编号
    */
    @TableField("key")
    private SettingEnum key;

    /**
    * 配置分类编号
    */
    @TableField("type")
    private String type;

    /**
    * 配置值
    */
    @TableField("value")
    private String value;

    /**
    * 配置备注，配置名称和使用说明
    */
    @TableField("remark")
    private String remark;

    /**
    * 状态 是否开启
    */
    @TableField("status")
    private Boolean status;

    /**
    * 序号
    */
    @TableField("sort")
    private Integer sort;


    public static final String KEY = "key";

    public static final String TYPE = "type";

    public static final String VALUE = "value";

    public static final String REMARK = "remark";

    public static final String STATUS = "status";

    public static final String SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }

}