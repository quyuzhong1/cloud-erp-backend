package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 系统配置管理
 * </p>
 *
 * @author lrp
 * @since 2024-07-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "cfg_setting", autoResultMap = true)
public class PlmCfgSettingEntity extends BaseEntity<PlmCfgSettingEntity> {

    /**
    * Key值
    */
    @TableField("key")
    private String key;
    /**
    * json数据
    */
    @TableField(value = "data_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> dataJson;
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