package com.erp.model.oms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * KOL回片费用表
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_feedback_cost")
public class KolFeedbackCostEntity extends BaseEntity<KolFeedbackCostEntity> {

    /**
    * 回片链接（完整链接）
    */
    @TableField("url")
    private String url;
    /**
    * 回片链接哈希值（MD5或SHA256，用于唯一键）
    */
    @TableField("url_hash")
    private String urlHash;
    /**
    * 费用名称
    */
    @TableField("cost_type")
    private String costType;
    /**
    * 费用名称ID
    */
    @TableField("cost_type_id")
    private String costTypeId;
    /**
    * 付费币别
    */
    @TableField("currency")
    private String currency;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 金额（原币）
    */
    @TableField("original_amount")
    private BigDecimal originalAmount;
    /**
    * 金额（本位币）
    */
    @TableField("base_amount")
    private BigDecimal baseAmount;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String URL = "url";

    public static final String URL_HASH = "url_hash";

    public static final String COST_TYPE = "cost_type";

    public static final String COST_TYPE_ID = "cost_type_id";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String ORIGINAL_AMOUNT = "original_amount";

    public static final String BASE_AMOUNT = "base_amount";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}