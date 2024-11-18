package com.erp.model.tms.entity;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;


/**
 * <p>
 * 系统配置管理
 * </p>
 *
 * @author zdy
 * @since 2024-02-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_setting")
public class CfgSettingEntity extends BaseEntity<CfgSettingEntity> {

    /**
    * Key值
    */
    @TableField("key")
    private String key;
    /**
     * json数据
     */
    @TableField(value = "data_json", jdbcType = JdbcType.OTHER)
    private JSONObject dataJson;
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


    public static final String FIELD_KEY = "key";

    public static final String DATA_JSON = "data_json";

    public static final String FIELD_DISABLED = "disabled";

    public static final String FIELD_INDEX = "index";

    public static final String FIELD_REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}