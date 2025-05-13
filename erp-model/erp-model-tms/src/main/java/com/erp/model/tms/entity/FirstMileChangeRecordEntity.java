package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * <p>
 * 头程调整记录
 * </p>
 *
 * @author zdy
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_change_record")
public class FirstMileChangeRecordEntity extends BaseEntity<FirstMileChangeRecordEntity> {

    /**
    * 调整单号
    */
    @TableField("code")
    private String code;
    /**
    * 调整类型:firstMileCost=费用调整,firstMileWeight=重量调整,thirdReceive=签收调整  枚举：FirstMileChangeRecordSourceTypeEnum
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 操作类型:manual=人工调整,auto=系统计算  枚举：FirstMileChangeRecordTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 修改范围:current=仅修改当前值,box=修改同箱同SKU,order=修改同单同SKU  枚举：FirstMileChangeRecordChangeRangeEnum
    */
    @TableField("change_range")
    private String changeRange;
    /**
    * 核算月份id
    */
    @TableField("report_period_id")
    private String reportPeriodId;
    /**
    * 核算月份
    */
    @TableField("report_period")
    private LocalDate reportPeriod;
    /**
    * 业务单号
    */
    @TableField("business_code")
    private String businessCode;
    /**
    * 发货单id
    */
    @TableField("delivery_id")
    private String deliveryId;
    /**
    * 发货单编码
    */
    @TableField("delivery_code")
    private String deliveryCode;
    /**
    * 物流运单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 平台skuNo
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * skuNO
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 调整分类:boxNo=箱号,shippingCost=运费,declareCost=关税,otherTaxFee=其他税费,otherCost=其他费用  枚举：FirstMileChangeRecordCategoryEnum
    */
    @TableField("category")
    private String category;
    /**
    * 调整字段:current_period_allocated_cost=本期分摊费用,mid_period_transit_cost=冲期初在途费用,end_period_transit_cost=期末在途费用,end_period_estimated_cost=期末暂估费用,charged_weight=出库重量  枚举：FirstMileChangeRecordCategoryFieldEnum
    */
    @TableField("category_field")
    private String categoryField;
    /**
    * 调整前数值
    */
    @TableField("old_value")
    private String oldValue;
    /**
    * 调整后数值
    */
    @TableField("new_value")
    private String newValue;
    /**
    * 是否最新记录
    */
    @TableField("is_latest")
    private Boolean isLatest;


    public static final String CODE = "code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String TYPE = "type";

    public static final String CHANGE_RANGE = "change_range";

    public static final String REPORT_PERIOD_ID = "report_period_id";

    public static final String REPORT_PERIOD = "report_period";

    public static final String BUSINESS_CODE = "business_code";

    public static final String DELIVERY_ID = "delivery_id";

    public static final String DELIVERY_CODE = "delivery_code";

    public static final String TRANSPORT_NO = "transport_no";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String CATEGORY = "category";

    public static final String CATEGORY_FIELD = "category_field";

    public static final String OLD_VALUE = "old_value";

    public static final String NEW_VALUE = "new_value";

    public static final String IS_LATEST = "is_latest";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
