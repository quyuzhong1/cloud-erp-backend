package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 截单开船配置
 * </p>
 *
 * @author will
 * @since 2024-03-15
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_cfg_sailing")
public class TmsCfgSailingEntity extends BaseEntity<TmsCfgSailingEntity> {

    /**
    * 物流商id
    */
    @TableField("logistics_supplier_id")
    private String logisticsSupplierId;
    /**
    * 物流商渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 日期值（每...周，每...月）
    */
    @TableField("date_value")
    private Integer dateValue;
    /**
    * 日期类型
    */
    @TableField("date_type")
    private String dateType;
    /**
    * 开船日期（周、月）
    */
    @TableField("start_date")
    private Integer startDate;
    /**
    * 开船日期时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;
    /**
    * 截单日期（周、月）
    */
    @TableField("end_date")
    private Integer endDate;
    /**
    * 截单日期时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;


    public static final String LOGISTICS_SUPPLIER_ID = "logistics_supplier_id";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String DATE_VALUE = "date_value";

    public static final String DATE_TYPE = "date_type";

    public static final String START_DATE = "start_date";

    public static final String START_TIME = "start_time";

    public static final String END_DATE = "end_date";

    public static final String END_TIME = "end_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}