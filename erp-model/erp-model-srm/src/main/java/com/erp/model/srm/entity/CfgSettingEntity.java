package com.erp.model.srm.entity;

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
import org.apache.ibatis.type.JdbcType;


/**
 * <p>
 * 系统配置管理
 * </p>
 *
 * @author zdy
 * @since 2024-01-10
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
    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;


    public static final String KEY = "key";

    public static final String DATA_JSON = "data_json";

    public static final String DISABLED = "disabled";

    public static final String INDEX = "index";

    public static final String REMARK = "remark";

    public static final String SUPPLIER_ID = "supplier_id";

    @Override
    public Serializable pkVal() {
        return null;
    }
}