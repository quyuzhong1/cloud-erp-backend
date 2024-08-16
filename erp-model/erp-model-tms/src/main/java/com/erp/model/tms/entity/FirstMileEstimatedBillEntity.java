package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 
 * @date 2024-08-16
 * @author tanmujin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_estimated_bill")
public class FirstMileEstimatedBillEntity extends BaseEntity<FirstMileEstimatedBillEntity> {
    /**
     * 暂估账单状态
     */
    @TableField("status")
    private String status;

    /**
     * 账单确认时间
     */
    @TableField("confirm_time")
    private LocalDateTime confirmTime;
}
