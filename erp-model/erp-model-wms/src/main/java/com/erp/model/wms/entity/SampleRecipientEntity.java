package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 样品领用单
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sample_recipient")
public class SampleRecipientEntity extends BaseEntity<SampleRecipientEntity> {

    /**
     * 审批状态(waitSubmit=待提交, approved=已批准, rejected=已驳回)
     */
    @TableField("approve_status")
    private String approveStatus;

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
     * 审批时间
     */
    @TableField("approve_time")
    private Date approveTime;

    /**
     * 作废状态(false:有效,true:已作废)
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

    /**
     * 作废时间
     */
    @TableField("invalid_time")
    private Date invalidTime;

    /**
     * 样品领用单号
     */
    @TableField("code")
    private String code;

    /**
     * 领用日期
     */
    @TableField("recipient_date")
    private Date recipientDate;

    /**
     * 用途 枚举类型：办公领用/拍摄/研发/抖音直播/客户领用（客户使用指导）/参展/营销样品/认证检测/供应链生产组装/用户新品体验（仓库提供）/不良品分析（从售后仓领样）/星河线下店领用/其他
     */
    @TableField("usage")
    private String usage;

    /**
     * 发货仓库ID
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 单据状态
     */
    @TableField("status")
    private String status;

    /**
     * 领用人ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 领用人姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 领用部门ID
     */
    @TableField("dept_id")
    private String deptId;

    /**
     * 领料组织ID
     */
    @TableField("pick_org_id")
    private String pickOrgId;

    /**
     * 领料组织名称
     */
    @TableField("pick_org_name")
    private String pickOrgName;

    /**
     * 使用方式 公司内部使用/公司外部使用
     */
    @TableField("usage_scope")
    private String usageScope;

    /**
     * 使用方id
     */
    @TableField("use_user_id")
    private String useUserId;

    /**
     * 使用方名称
     */
    @TableField("use_user_name")
    private String useUserName;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否邮寄
     */
    @TableField("is_delivery")
    private Boolean isDelivery;

    /**
     * 收货地址
     */
    @TableField("receive_address")
    private String receiveAddress;

    /**
     * 收货人
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 联系电话
     */
    @TableField("receive_phone")
    private String receivePhone;

    /**
     * 来源ID（预留字段）
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源单号（预留字段）
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 来源类型（预留字段）
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * SKU成本合计
     */
    @TableField("sku_total_cost")
    private BigDecimal skuTotalCost;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String INVALID_TIME = "invalid_time";

    public static final String CODE = "code";

    public static final String RECIPIENT_DATE = "recipient_date";

    public static final String USAGE = "usage";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String STATUS = "status";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String DEPT_ID = "dept_id";

    public static final String PICK_ORG_ID = "pick_org_id";

    public static final String PICK_ORG_NAME = "pick_org_name";

    public static final String USAGE_SCOPE = "usage_scope";

    public static final String USE_USER_ID = "use_user_id";

    public static final String USE_USER_NAME = "use_user_name";

    public static final String REMARK = "remark";

    public static final String IS_DELIVERY = "is_delivery";

    public static final String RECEIVE_ADDRESS = "receive_address";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String RECEIVE_PHONE = "receive_phone";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SKU_TOTAL_COST = "sku_total_cost";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
