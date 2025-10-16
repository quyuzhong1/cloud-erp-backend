package com.erp.model.srm.entity;

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
 * 
 * </p>
 *
 * @author will
 * @since 2025-09-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("payable_info")
public class PayableInfoEntity extends BaseEntity<PayableInfoEntity> {

    /**
    * 应付单号
    */
    @TableField("code")
    private String code;
    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 业务日期
    */
    @TableField("date")
    private LocalDate date;
    /**
    * 组织id
    */
    @TableField("org_id")
    private String orgId;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 审核人名称
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 审核人id
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 来源类型，取采购对账单
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源编码
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 业务类型,payableType字典
    */
    @TableField("type")
    private String type;
    /**
    * 三方系统id
    */
    @TableField("third_payable_id")
    private String thirdPayableId;
    /**
    * 三方系统编码
    */
    @TableField("third_payable_code")
    private String thirdPayableCode;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;


    public static final String CODE = "code";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String DATE = "date";

    public static final String ORG_ID = "org_id";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String REMARK = "remark";

    public static final String TYPE = "type";

    public static final String THIRD_PAYABLE_ID = "third_payable_id";

    public static final String THIRD_PAYABLE_CODE = "third_payable_code";

    public static final String APPROVE_STATUS = "approve_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}