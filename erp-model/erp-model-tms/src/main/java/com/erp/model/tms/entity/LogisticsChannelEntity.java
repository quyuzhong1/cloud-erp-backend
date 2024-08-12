package com.erp.model.tms.entity;

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
 * 物流渠道表
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_channel")
public class LogisticsChannelEntity extends BaseEntity<LogisticsChannelEntity> {

    /**
    * 来源id
     * 物流仓库表id logistics_warehouse'
    */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源类型
     * logisticsWarehouse
     */
    @TableField("source_type")
    private String sourceType;


    @TableField("main_id")
    private String mainId;
    /**
    * 渠道名称
    */
    @TableField("name")
    private String name;
    /**
    * 渠道代码
    */
    @TableField("code")
    private String code;
    /**
    * 时效
    */
    @TableField("effective_time")
    private String effectiveTime;


    /**
    * 时效单位
    */
    @TableField("effective_time_unit")
    private String effectiveTimeUnit;
    /**
    * 物流轨迹查询方式
    */
    @TableField("track_query_mode")
    private String trackQueryMode;

    @TableField("paper_size")
    private String paperSize;
    /**
    * 纸张长
    */
    @TableField("paper_length")
    private Integer paperLength;
    /**
    * 纸张宽
    */
    @TableField("paper_width")
    private Integer paperWidth;
    /**
    * 分拣码
    */
    @TableField("sorting_code")
    private String sortingCode;

    /**
    * 费用规则
    */
    @TableField("fee_rule")
    private String feeRule;


    /**
    * 最高报关金额
    */
    @TableField("max_customs_amount")
    private BigDecimal maxCustomsAmount;
    /**
    * 最高报关币别
    */
    @TableField("max_customs_currency")
    private String maxCustomsCurrency;
    /**
    * 最低报关金额
    */
    @TableField("min_customs_amount")
    private BigDecimal minCustomsAmount;
    /**
    * 最低报关币种
    */
    @TableField("min_customs_currency")
    private String minCustomsCurrency;
    /**
    * 重量上限
    */
    @TableField("max_weight")
    private BigDecimal maxWeight;
    /**
    * 重量单位
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
     * 长度上限
     */
    @TableField("max_length")
    private BigDecimal maxLength;
    /**
     * 长度单位
     */
    @TableField("size_unit")
    private String sizeUnit;
    /**
     * 宽度上限
     */
    @TableField("max_width")
    private BigDecimal maxWidth;
    /**
     * 高度上限
     */
    @TableField("max_height")
    private BigDecimal maxHeight;
    /**
    * 税费模式
    */
    @TableField("tax_model")
    private String taxModel;
    /**
    * 是否ioss 预交
    */
    @TableField("is_ioss_prepay")
    private Boolean isIossPrepay;
    /**
    * 是否签名服务
    */
    @TableField("is_api_sign")
    private Boolean isApiSign;
    /**
    * 是否保险
    */
    @TableField("is_api_insurance")
    private Boolean isApiInsurance;


    /**
     * 是否禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 物流商同步的source id 来源 logistics_sale_channel 表id
     */
    @TableField("sync_source_id")
    private String syncSourceId;

    /**
     * 报关单号类型（transportNo运单号、trackNo跟踪号）
     */
    @TableField("declare_code_type")
    private String declareCodeType;

    /**
     * 配送方式/发货方式（上门揽收DOOR_PICKUP, 自寄SELF_POST, 自送SELF_SEND）
     * DeliveryTypeEnum
     * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = deliveryType
     */
    @TableField("delivery_type")
    private String deliveryType;
    /**
     * 不可达处理 退回:return/销毁:return 默认 return销毁
     * UnDeliverableDecisionEnum
     * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = undeliverableDecision
     */
    @TableField("undeliverable_decision")
    private String undeliverableDecision;
    /**
     * 轨迹查询单号（运单号transportNo跟踪号trackNo）
     * TrackQueryTypeEnum
     * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = trackQueryType
     */
    @TableField("track_query_type")
    private String trackQueryType;

    /**
     * 材积设置
     */
    @TableField("volume_setting")
    private Integer volumeSetting;

    public static final String MAIN_ID = "main_id";

    public static final String NAME = "name";

    public static final String CODE = "code";

    public static final String EFFECTIVE_TIME = "effective_time";

    public static final String EFFECTIVE_TIME_UNIT = "effective_time_unit";

    public static final String TRACK_QUERY_MODE = "track_query_mode";

    public static final String PAPER_LENGTH = "paper_length";

    public static final String PAPER_WIDTH = "paper_width";

    public static final String SORTING_CODE = "sorting_code";

    public static final String FEE_RULE = "fee_rule";

    public static final String MAX_CUSTOMS_AMOUNT = "max_customs_amount";

    public static final String MAX_CUSTOMS_CURRENCY = "max_customs_currency";

    public static final String MIN_CUSTOMS_AMOUNT = "min_customs_amount";

    public static final String MIN_CUSTOMS_CURRENCY = "min_customs_currency";

    public static final String MAX_WEIGHT = "max_weight";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String TAX_MODEL = "tax_model";

    public static final String IS_IOSS_PREPAY = "is_ioss_prepay";

    public static final String IS_API_SIGN = "is_api_sign";

    public static final String IS_API_INSURANCE = "is_api_insurance";

    @Override
    public Serializable pkVal() {
        return null;
    }

}