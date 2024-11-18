package com.erp.model.wms.entity;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 系统配置管理
 * </p>
 *
 * @author will
 * @since 2024-01-08
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
    @TableField(value = "data_json", typeHandler = JacksonTypeHandler.class)
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

    @Override
    public Serializable pkVal() {
        return null;
    }

}