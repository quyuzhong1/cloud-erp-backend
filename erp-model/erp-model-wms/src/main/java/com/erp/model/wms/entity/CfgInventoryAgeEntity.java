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
 * 库龄配置表
 * </p>
 *
 * @author will
 * @since 2025-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_inventory_age")
public class CfgInventoryAgeEntity extends BaseEntity<CfgInventoryAgeEntity> {

    /**
    * 人员id
    */
    @TableField("user_id")
    private String userId;
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
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String USER_ID = "user_id";

    public static final String DATA_JSON = "data_json";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}