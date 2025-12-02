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
 * B2B寄养申请主表
 * </p>
 *
 * @author will
 * @since 2025-12-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_b2b_application")
public class KolB2bApplicationEntity extends BaseEntity<KolB2bApplicationEntity> {

    /**
    * 申请单号
    */
    @TableField("code")
    private String code;
    /**
    * 申请日期
    */
    @TableField("date")
    private LocalDate date;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
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
    * 作废状态
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
    private LocalDateTime invalidTime;
    /**
    * 寄样类型
    */
    @TableField("type")
    private String type;
    /**
    * 客户id
    */
    @TableField("customer_id")
    private String customerId;
    /**
    * 申请说明
    */
    @TableField("apply_remark")
    private String applyRemark;
    /**
    * 申请人id
    */
    @TableField("apply_user_id")
    private String applyUserId;
    /**
    * 申请部门id
    */
    @TableField("apply_dept_id")
    private String applyDeptId;
    /**
    * 收货国家编码
    */
    @TableField("country_id")
    private String countryId;
    /**
    * 收货国家名称
    */
    @TableField("country_name")
    private String countryName;
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
    * 地址类型，CustomerAddressTypeEnum枚举
    */
    @TableField("address_type")
    private String addressType;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String CODE = "code";

    public static final String DATE = "date";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String INVALID_TIME = "invalid_time";

    public static final String TYPE = "type";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String APPLY_REMARK = "apply_remark";

    public static final String APPLY_USER_ID = "apply_user_id";

    public static final String APPLY_DEPT_ID = "apply_dept_id";

    public static final String COUNTRY_ID = "country_id";

    public static final String COUNTRY_NAME = "country_name";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String TEL_NUMBER = "tel_number";

    public static final String RECEIVE_ADDRESS = "receive_address";

    public static final String ADDRESS_TYPE = "address_type";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}