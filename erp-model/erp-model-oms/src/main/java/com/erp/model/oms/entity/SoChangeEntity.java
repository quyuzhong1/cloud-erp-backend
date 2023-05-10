package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 销售订单变更
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_change")
public class SoChangeEntity extends BaseEntity<SoChangeEntity> {

    /**
     * code
     */
    @TableField("code")
    private String code;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 客户id
     */
    @TableField("customer_id")
    private String customerId;

    /**
     * 收货人
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 联系人电话
     */
    @TableField("tel_number")
    private String telNumber;

    /**
     * 收货地址
     */
    @TableField("receive_address")
    private String receiveAddress;

    /**
     * 交货方式
     */
    @TableField("delivery_mode")
    private String deliveryMode;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 是否含税 true
     */
    @TableField("is_tax")
    private Boolean isTax;

    /**
     * 变更日期
     */
    @TableField("bill_date")
    private Date billDate;

    /**
     * 订单id
     */
    @TableField("so_id")
    private String soId;

    /**
     * 变更人id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 变更人
     */
    @TableField("user_name")
    private String userName;

    /**
     * 变更部门id
     */
    @TableField("dept_id")
    private String deptId;

    /**
     * 变更部门
     */
    @TableField("dept_name")
    private String deptName;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String TEL_NUMBER = "tel_number";

    public static final String RECEIVE_ADDRESS = "receive_address";

    public static final String DELIVERY_MODE = "delivery_mode";

    public static final String CURRENCY = "currency";

    public static final String IS_TAX = "is_tax";

    public static final String BILL_DATE = "bill_date";

    public static final String SO_ID = "so_id";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String DEPT_ID = "dept_id";

    public static final String DEPT_NAME = "dept_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
