package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.time.LocalTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 波次规则执行记录表
 * </p>
 *
 * @author will
 * @since 2024-07-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_wave_record")
public class CfgRuleWaveRecordEntity extends BaseEntity<CfgRuleWaveRecordEntity> {

    /**
    * 波次规则id
    */
    @TableField("rule_wave_id")
    private String ruleWaveId;
    /**
    * 返回信息
    */
    @TableField("return_msg")
    private String returnMsg;
    /**
    * 执行时间
    */
    @TableField("execution_time")
    private LocalTime executionTime;


    public static final String RULE_WAVE_ID = "rule_wave_id";

    public static final String RETURN_MSG = "return_msg";

    public static final String EXECUTION_TIME = "execution_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}