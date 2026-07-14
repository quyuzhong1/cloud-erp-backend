package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_logistics_track_webhook_record")
public class DmpLogisticsTrackWebhookRecordEntity extends BaseEntity<DmpLogisticsTrackWebhookRecordEntity> {

    @TableField("platform_code")
    private String platformCode;

    @TableField("track_no")
    private String trackNo;

    @TableField("status")
    private String status;

    @TableField("raw_data")
    private String rawData;

    @TableField("remark")
    private String remark;

    public static final String PLATFORM_CODE = "platform_code";

    public static final String TRACK_NO = "track_no";

    public static final String STATUS = "status";

    public static final String RAW_DATA = "raw_data";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }
}
