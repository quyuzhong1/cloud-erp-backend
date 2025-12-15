package com.erp.model.oms.entity;

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
 * B2C寄样申请单
 * </p>
 *
 * @author jack
 * @since 2025-12-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_b2c_application")
public class KolB2cApplicationEntity extends BaseEntity<KolB2cApplicationEntity> {

    /**
    * 作废状态
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 作废备注
    */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 审批状态
    */
    @TableField("approve_status")
    private String approveStatus;
    /**
    * 审批时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 审批人ID
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审批人姓名
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 申请单号
    */
    @TableField("code")
    private String code;
    /**
    * 申请日期
    */
    @TableField("apply_date")
    private LocalDate applyDate;
    /**
    * 寄样类型
    */
    @TableField("sample_type")
    private String sampleType;
    /**
    * 是否国际
    */
    @TableField("is_international")
    private Boolean isInternational;
    /**
    * 店铺ID
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 币别
    */
    @TableField("currency")
    private String currency;
    /**
    * 发货仓库ID
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 发货仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 物流渠道
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
    * 申请人ID
    */
    @TableField("apply_user_id")
    private String applyUserId;
    /**
    * 申请人姓名
    */
    @TableField("apply_user_name")
    private String applyUserName;
    /**
    * 申请部门ID
    */
    @TableField("apply_dept_id")
    private String applyDeptId;
    /**
    * 申请部门名称
    */
    @TableField("apply_dept_name")
    private String applyDeptName;
    /**
    * 申请理由
    */
    @TableField("apply_remark")
    private String applyRemark;


    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String REMARK = "remark";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String CODE = "code";

    public static final String APPLY_DATE = "apply_date";

    public static final String SAMPLE_TYPE = "sample_type";

    public static final String IS_INTERNATIONAL = "is_international";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String CURRENCY = "currency";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String APPLY_USER_ID = "apply_user_id";

    public static final String APPLY_USER_NAME = "apply_user_name";

    public static final String APPLY_DEPT_ID = "apply_dept_id";

    public static final String APPLY_DEPT_NAME = "apply_dept_name";

    public static final String APPLY_REMARK = "apply_remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}