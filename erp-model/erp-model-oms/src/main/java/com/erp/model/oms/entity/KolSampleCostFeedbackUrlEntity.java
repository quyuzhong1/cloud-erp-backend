package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 寄样费用回片链接
 * </p>
 *
 * @author codex
 * @since 2026-05-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_sample_cost_ref_feedback_url")
public class KolSampleCostFeedbackUrlEntity extends BaseEntity<KolSampleCostFeedbackUrlEntity> {

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源主单ID
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源单号
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 来源明细ID
     */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
     * 回片登记ID
     */
    @TableField("feedback_id")
    private String feedbackId;

    /**
     * 达人ID
     */
    @TableField("partner_id")
    private String partnerId;

    /**
     * 达人昵称
     */
    @TableField("partner_nickname")
    private String partnerNickname;

    /**
     * 回片链接
     */
    @TableField("url")
    private String url;

    /**
     * 回片链接哈希
     */
    @TableField("url_hash")
    private String urlHash;

    /**
     * 回片状态
     */
    @TableField("feedback_status")
    private String feedbackStatus;

    /**
     * 同一来源明细下的展示顺序
     */
    @TableField("sort")
    private Integer sort;

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String FEEDBACK_ID = "feedback_id";

    public static final String PARTNER_ID = "partner_id";

    public static final String PARTNER_NICKNAME = "partner_nickname";

    public static final String URL = "url";

    public static final String URL_HASH = "url_hash";

    public static final String FEEDBACK_STATUS = "feedback_status";

    public static final String SORT = "sort";
}
