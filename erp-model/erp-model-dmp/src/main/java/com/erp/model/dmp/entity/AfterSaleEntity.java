package com.erp.model.dmp.entity;

import java.math.BigDecimal;
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
 * 售后申请表
 * </p>
 *
 * @author jack
 * @since 2025-04-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("after_sale")
public class AfterSaleEntity extends BaseEntity<AfterSaleEntity> {

    /**
    * 工单号
    */
    @TableField("code")
    private String code;
    /**
    * 单据状态
    */
    @TableField("status")
    private String status;
    /**
    * 单据日期
    */
    @TableField("bill_date")
    private LocalDate billDate;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 最新审核人ID
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 最新审核人
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * thrid_user_info主键id
    */
    @TableField("thrid_user_id")
    private String thridUserId;
    /**
    * 第三方平台类型
    */
    @TableField("thrid_type")
    private String thridType;
    /**
    * 平台订单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 销售平台
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 购买日期
    */
    @TableField("buy_date")
    private LocalDate buyDate;
    /**
    * 邮寄地址
    */
    @TableField("address")
    private String address;
    /**
    *  故障描述
    */
    @TableField("fault_desc")
    private String faultDesc;
    /**
    * 旺店通绑定维修收费单号
    */
    @TableField("repair_invoice_code")
    private String repairInvoiceCode;
    /**
    * 货值
    */
    @TableField("total_price")
    private BigDecimal totalPrice;
    /**
    * 维修金额
    */
    @TableField("total_repair_amount")
    private BigDecimal totalRepairAmount;
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
    * 作废时间
    */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
     * 姓名
     */
    @TableField("username")
    private String username;
    /**
     * 手机号码
     */
    @TableField("phone_number")
    private String phoneNumber;

    /**
     * 客服备注
     */
    @TableField("csr_remark")
    private String csrRemark;

    /**
     * 维修备注
     */
    @TableField("rma_remark")
    private String rmaRemark;


    public static final String CODE = "code";

    public static final String STATUS = "status";

    public static final String BILL_DATE = "bill_date";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String THRID_USER_ID = "thrid_user_id";

    public static final String THRID_TYPE = "thrid_type";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String BUY_DATE = "buy_date";

    public static final String ADDRESS = "address";

    public static final String FAULT_DESC = "fault_desc";

    public static final String REPAIR_INVOICE_CODE = "repair_invoice_code";

    public static final String TOTAL_PRICE = "total_price";

    public static final String TOTAL_REPAIR_AMOUNT = "total_repair_amount";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String INVALID_TIME = "invalid_time";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}