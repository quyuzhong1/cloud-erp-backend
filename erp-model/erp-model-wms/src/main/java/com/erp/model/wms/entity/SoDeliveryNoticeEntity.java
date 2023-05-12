package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 发货通知单主表明细表
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_delivery_notice")
public class SoDeliveryNoticeEntity extends BaseEntity<SoDeliveryNoticeEntity> {

    /**
     * 审核状态 
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 单据类型
     */
    @TableField("type")
    private String type;

    /**
     * 销售组织表id
     */
    @TableField("sales_org_id")
    private String salesOrgId;

    /**
     * 销售组织名称
     */
    @TableField("sales_org_name")
    private String salesOrgName;

    /**
     * 销售部门id
     */
    @TableField("sales_dept_id")
    private String salesDeptId;

    /**
     * 销售部门名称
     */
    @TableField("sales_dept_name")
    private String salesDeptName;

    /**
     * 销售员id
     */
    @TableField("seller_id")
    private String sellerId;

    /**
     * 销售员名称
     */
    @TableField("seller_name")
    private String sellerName;

    /**
     * 发货组织id
     */
    @TableField("delivery_org_id")
    private String deliveryOrgId;

    /**
     * 发货组织名称
     */
    @TableField("delivery_org_name")
    private String deliveryOrgName;

    /**
     * 要货日期
     */
    @TableField("require_date")
    private LocalDate requireDate;

    /**
     * 预计发货日期
     */
    @TableField("plan_delivery_date")
    private LocalDate planDeliveryDate;

    /**
     * 完成打包日期
     */
    @TableField("pack_date")
    private LocalDate packDate;

    /**
     * 实际发货日期
     */
    @TableField("actual_delivery_date")
    private LocalDate actualDeliveryDate;

    /**
     * 承运商id
     */
    @TableField("carrier_id")
    private String carrierId;

    /**
     * 承运商名称
     */
    @TableField("carrier_name")
    private String carrierName;

    /**
     * 运输单号
     */
    @TableField("track_no")
    private String trackNo;

    /**
     * 承运商名称
     */
    @TableField("warehouse_id")
    private String WarehouseId;

    /**
     * 承运商名称
     */
    @TableField("warehouse_id")
    private String WarehouseName;

    /**
     * 客户表id
     */
    @TableField("customer_id")
    private String customerId;

    /**
     * 客户名称
     */
    @TableField("customer_name")
    private String customerName;

    /**
     * 收货人
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 联系电话
     */
    @TableField("tel_number")
    private String telNumber;

    /**
     * 收货地址
     */
    @TableField("receive_address")
    private String receiveAddress;

    /**
     * 交货方式  dict_basic表type = deliveryMode  deliverGoods（发货）selfExtraction（自提）
     */
    @TableField("delivery_mode_dict")
    private String deliveryModeDict;

    /**
     * 作废状态
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
     * 作废描述
     */
    @TableField("invalid_remark")
    private String invalidRemark;

    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源编号
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 发货状态 wms/common/enumDropDown?type=DeliveryStatus
     * "unShipped","未发货"
     * "partialShipment","部分发货"
     * "completeShipment","已发货"
     */
    @TableField("delivery_status_dict")
    private String deliveryStatusDict;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String TYPE = "type";

    public static final String SALES_ORG_ID = "sales_org_id";

    public static final String SALES_ORG_NAME = "sales_org_name";

    public static final String DEPT_ID = "dept_id";

    public static final String DEPT_NAME = "dept_name";

    public static final String SELLER_ID = "seller_id";

    public static final String SELLER_NAME = "seller_name";

    public static final String DELIVERY_ORG_ID = "delivery_org_id";

    public static final String DELIVERY_ORG_NAME = "delivery_org_name";

    public static final String REQUIRE_DATE = "require_date";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    public static final String FINISH_PACK_DATE = "finish_pack_date";

    public static final String ACTUAL_DELIVERY_DATE = "actual_delivery_date";

    public static final String CARRIER_ID = "carrier_id";

    public static final String CARRIER_NAME = "carrier_name";

    public static final String TRACK_NO = "track_no";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String CUSTOMER_NAME = "customer_name";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String TEL_NUMBER = "tel_number";

    public static final String RECEIVE_ADDRESS = "receive_address";

    public static final String DELIVERY_MODE_DICT = "delivery_mode_dict";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
