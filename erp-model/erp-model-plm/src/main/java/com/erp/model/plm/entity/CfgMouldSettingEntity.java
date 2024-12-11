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
 * 模具配置
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_mould_setting")
public class CfgMouldSettingEntity extends BaseEntity<CfgMouldSettingEntity> {

    /**
     * 类型 模具/文档
     */
    @TableField("type")
    private String type;
    /**
     * 名字
     */
    @TableField("name")
    private String name;
    /**
     * 排序
     */
    @TableField("index")
    private Integer index;


    public static final String TYPE = "type";


    public static final String NAME = "name";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}