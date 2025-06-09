package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 最近建议明细
 * </p>
 *
 * @author liaohui
 * @since 2024-09-25
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("recent_suggestion_detail_history")
@EqualsAndHashCode(callSuper = true)
public class RecentSuggestionDetailHistoryEntity extends BaseEntity<RecentSuggestionDetailHistoryEntity> {

    private static final long serialVersionUID = -1794246094903949117L;
    /**
     * 补货建议id
     */
    @TableField("replenishment_detail_id")
    private String replenishmentDetailId;

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * 天数
     */
    @TableField("days")
    private Integer days;

    /**
     * 补货建议标识类型
     */
    @TableField("mark_type")
    private String markType;

    /**
     * 建议数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 日期
     */
    @TableField("date")
    private Date date;

    /**
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;


    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String TYPE = "type";

    public static final String DAYS = "days";

    public static final String MARK_TYPE = "mark_type";

    public static final String QTY = "qty";

    public static final String DATE = "date";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
