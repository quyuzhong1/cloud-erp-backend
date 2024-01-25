package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.time.LocalTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 截单设置
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare_deadline_setting")
public class TransferDeclareDeadlineSettingEntity extends BaseEntity<TransferDeclareDeadlineSettingEntity> {

    /**
    * 截单时间
    */
    @TableField("deadline_time")
    private LocalDateTime deadlineTime;
    /**
    * 生成时间（时：分） 例：12:30
    */
    @TableField("generate_time")
    private LocalTime generateTime;


    public static final String DEADLINE_TIME = "deadline_time";

    public static final String GENERATE_TIME = "generate_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}