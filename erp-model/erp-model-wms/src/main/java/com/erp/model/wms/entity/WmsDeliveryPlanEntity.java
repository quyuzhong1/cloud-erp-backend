package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 发货计划
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wms_delivery_plan")
public class WmsDeliveryPlanEntity extends BaseEntity<WmsDeliveryPlanEntity> {

    /**
    * code
    */
    @TableField("code")
    private String code;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审核人
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审核人中文名
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 作废状态
    */
    @TableField("invalid_status")
    private String invalidStatus;
    /**
    * 作废备注
    */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
    * 作废时间
    */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;
    /**
    * 发货状态
    */
    @TableField("delivery_status")
    private String deliveryStatus;
    /**
    * 目的仓id
    */
    @TableField("to_warehouse_id")
    private String toWarehouseId;
    /**
    * 目的仓中文名
    */
    @TableField("to_warehouse_name")
    private String toWarehouseName;
    /**
    * 国家二字码
    */
    @TableField("country")
    private String country;
    /**
    * 国家中文名
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 计划发货时间
    */
    @TableField(value = "plan_delivery_date", fill = FieldFill.INSERT_UPDATE)
    private LocalDate planDeliveryDate;
    /**
    * 描述
    */
    @TableField("remark")
    private String remark;

    /**
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 店铺名称
     */
    @TableField("shop_name")
    private String shopName;

    /**
     * 单据类型:fba,thirdWarehouse
     * {@link com.erp.model.wms.enums.DeliveryPlanTypeEnum}
     */
    @TableField("type")
    private String type;

    

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String INVALID_TIME = "invalid_time";

    public static final String DELIVERY_STATUS = "delivery_status";

    public static final String TO_WAREHOUSE_ID = "to_warehouse_id";

    public static final String TO_WAREHOUSE_NAME = "to_warehouse_name";

    public static final String COUNTRY_NAME = "country_name";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    

    @Override
    public Serializable pkVal() {
        return null;
    }

}