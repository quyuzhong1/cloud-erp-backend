package com.erp.model.dmp.entity;

import java.math.BigDecimal;
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
 * 第三方汇率
 * </p>
 *
 * @author shukai
 * @since 2024-08-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_exchange_rate")
public class DmpThirdExchangeRateEntity extends BaseEntity<DmpThirdExchangeRateEntity> {

    /**
    * 来源系统
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 生效日期
    */
    @TableField("settlement_date_begin")
    private LocalDateTime settlementDateBegin;
    /**
    * 失效日期
    */
    @TableField("settlement_date_end")
    private LocalDateTime settlementDateEnd;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 源币种
    */
    @TableField("source_currency_code")
    private String sourceCurrencyCode;
    /**
    * 目标币种
    */
    @TableField("target_currency_code")
    private String targetCurrencyCode;
    /**
    * 汇率类型
    */
    @TableField("type")
    private String type;
    /**
    * 间接汇率
    */
    @TableField("indirect_exchange_rate")
    private BigDecimal indirectExchangeRate;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 审核日期
    */
    @TableField("approve_date")
    private LocalDateTime approveDate;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private String approveStatus;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 禁用日期
    */
    @TableField("disabled_date")
    private LocalDateTime disabledDate;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;


    public static final String SOURCE_SYSTEM = "source_system";

    public static final String SETTLEMENT_DATE_BEGIN = "settlement_date_begin";

    public static final String SETTLEMENT_DATE_END = "settlement_date_end";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String SOURCE_CURRENCY_CODE = "source_currency_code";

    public static final String TARGET_CURRENCY_CODE = "target_currency_code";

    public static final String TYPE = "type";

    public static final String INDIRECT_EXCHANGE_RATE = "indirect_exchange_rate";

    public static final String SOURCE_ID = "source_id";

    public static final String APPROVE_DATE = "approve_date";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String DISABLED = "disabled";

    public static final String DISABLED_DATE = "disabled_date";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    @Override
    public Serializable pkVal() {
        return null;
    }

}