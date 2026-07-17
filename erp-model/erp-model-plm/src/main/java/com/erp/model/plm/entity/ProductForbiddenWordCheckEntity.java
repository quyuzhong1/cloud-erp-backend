package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 产品违禁词检测记录
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("product_forbidden_word_check")
public class ProductForbiddenWordCheckEntity extends BaseEntity<ProductForbiddenWordCheckEntity> {

    /**
     * 检测报告名称
     */
    @TableField("report_name")
    private String reportName;

    /**
     * 检测报告 FastDFS 地址
     */
    @TableField(value = "report_url", updateStrategy = FieldStrategy.IGNORED)
    private String reportUrl;

    /**
     * 报告日期，格式 yyyyMMdd
     */
    @TableField("report_date")
    private String reportDate;

    /**
     * 当日报告序列号
     */
    @TableField("report_seq")
    private Integer reportSeq;

    /**
     * 状态：0待检测 1检测中 2已检测 3检测失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 检测完成时间
     */
    @TableField(value = "finish_time", updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime finishTime;

    /**
     * 检测产品总数
     */
    @TableField("total_count")
    private Integer totalCount;

    /**
     * 命中产品数
     */
    @TableField("hit_count")
    private Integer hitCount;

    /**
     * 失败原因
     */
    @TableField(value = "fail_reason", updateStrategy = FieldStrategy.IGNORED)
    private String failReason;

    public static final String STATUS = "status";

    public static final String REPORT_DATE = "report_date";

    public static final String REPORT_SEQ = "report_seq";
}
