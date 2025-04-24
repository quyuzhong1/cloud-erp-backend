package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 面板打印设置表
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_print_type")
public class LogisticsPrintTypeEntity extends BaseEntity<LogisticsPrintTypeEntity> {

    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 打印类型
    */
    @TableField("print_type")
    private String printType;
    /**
    * 标签类型
    */
    @TableField("label_type")
    private String labelType;


    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String PRINT_TYPE = "print_type";

    public static final String LABEL_TYPE = "label_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}