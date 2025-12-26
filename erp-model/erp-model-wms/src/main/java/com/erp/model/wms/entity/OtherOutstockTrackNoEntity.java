package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 其他出库单跟踪号映射表
 * </p>
 *
 * @author system
 * @since 2025-12-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("other_outstock_track_no")
public class OtherOutstockTrackNoEntity extends BaseEntity<OtherOutstockTrackNoEntity> {

    /**
     * 其他出库单ID
     */
    @TableField("other_outstock_id")
    private String otherOutstockId;

    /**
     * 其他出库单编号
     */
    @TableField("other_outstock_code")
    private String otherOutstockCode;

    /**
     * 跟踪号(多个换行隔开)
     */
    @TableField("track_no")
    private String trackNo;

    /**
     * 其他出库单来源单号（从other_outstock表查询获取）
     */
    @TableField("other_outstock_source_code")
    private String otherOutstockSourceCode;

    public static final String OTHER_OUTSTOCK_ID = "other_outstock_id";

    public static final String OTHER_OUTSTOCK_CODE = "other_outstock_code";

    public static final String TRACK_NO = "track_no";

    public static final String OTHER_OUTSTOCK_SOURCE_CODE = "other_outstock_source_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
