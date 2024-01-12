package com.erp.model.srm.entity;

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
 * 送货单
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("delivery_order")
public class DeliveryOrderEntity extends BaseEntity<DeliveryOrderEntity> {

    /**
    * 送货单号
    */
    @TableField("code")
    private String code;
    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 预计到达日期
    */
    @TableField("expected_date")
    private LocalDateTime expectedDate;
    /**
    * 收货单号
    */
    @TableField("receive_code")
    private String receiveCode;
    /**
    * 来源订单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 客户名称
    */
    @TableField("customer_name")
    private String customerName;
    /**
    * 联系人id
    */
    @TableField("contact_id")
    private String contactId;
    /**
    * 联系人name
    */
    @TableField("contact_name")
    private String contactName;
    /**
    * 目的仓id
    */
    @TableField("to_warehouse_id")
    private String toWarehouseId;
    /**
    * 目的仓名称
    */
    @TableField("to_warehouse_name")
    private String toWarehouseName;
    /**
    * 打印日期
    */
    @TableField("print_date")
    private LocalDateTime printDate;
    /**
    * 确认收货日期
    */
    @TableField("confirm_receive_date")
    private LocalDateTime confirmReceiveDate;
    /**
    * 收货员id
    */
    @TableField("receive_user_id")
    private String receiveUserId;
    /**
    * 收货员名
    */
    @TableField("receive_user_name")
    private String receiveUserName;
    /**
    * 收货电话
    */
    @TableField("receive_phone")
    private String receivePhone;
    /**
    * 收货地址
    */
    @TableField("receive_address")
    private String receiveAddress;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 收货状态
    */
    @TableField("receipt_status")
    private String receiptStatus;
    /**
    * 是否打印
    */
    @TableField("is_print")
    private Boolean isPrint;


    public static final String CODE = "code";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SOURCE_ID = "source_id";

    public static final String EXPECTED_DATE = "expected_date";

    public static final String RECEIVE_CODE = "receive_code";

    public static final String SOURCE_CODE = "source_code";

    public static final String CUSTOMER_NAME = "customer_name";

    public static final String CONTACT_ID = "contact_id";

    public static final String CONTACT_NAME = "contact_name";

    public static final String TO_WAREHOUSE_ID = "to_warehouse_id";

    public static final String TO_WAREHOUSE_NAME = "to_warehouse_name";

    public static final String PRINT_DATE = "print_date";

    public static final String CONFIRM_RECEIVE_DATE = "confirm_receive_date";

    public static final String RECEIVE_USER_ID = "receive_user_id";

    public static final String RECEIVE_USER_NAME = "receive_user_name";

    public static final String RECEIVE_PHONE = "receive_phone";

    public static final String RECEIVE_ADDRESS = "receive_address";

    public static final String SOURCE_TYPE = "source_type";

    public static final String RECEIPT_STATUS = "receipt_status";

    public static final String IS_PRINT = "is_print";

    @Override
    public Serializable pkVal() {
        return null;
    }

}