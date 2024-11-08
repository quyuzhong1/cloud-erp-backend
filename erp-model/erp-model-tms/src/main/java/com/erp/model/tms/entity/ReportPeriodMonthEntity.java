package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 核算期间月份表
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("report_period_month")
public class ReportPeriodMonthEntity extends BaseEntity<ReportPeriodMonthEntity> {

    /**
    * 核算月份
    */
    @TableField("month")
    private LocalDate month;
    /**
    * 分摊组织id
    */
    @TableField("org_id")
    private String orgId;
    /**
    * 分摊组织名称
    */
    @TableField("org_name")
    private String orgName;


    public static final String MONTH = "month";

    public static final String ORG_ID = "org_id";

    public static final String ORG_NAME = "org_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}