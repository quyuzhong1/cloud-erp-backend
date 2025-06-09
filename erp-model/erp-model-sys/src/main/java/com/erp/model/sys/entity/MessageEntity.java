package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.LinkedHashMap;


/**
 * <p>
 * 消息通知表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-10
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "message", autoResultMap = true)
public class MessageEntity extends BaseEntity<MessageEntity> {

    /**
    * 消息类型
    */
    @TableField("type")
    private String type;

    /**
     * 数据集json
     */
    @TableField(value = "data_json", typeHandler= JacksonTypeHandler.class)
    private LinkedHashMap<String, Object> dataJson;

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
    * 应用类型：PDA、PC
    */
    @TableField("application")
    private String application;

    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String FIELD_TYPE = "type";

    public static final String DATA_JSON = "data_json";

    public static final String FIELD_REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}