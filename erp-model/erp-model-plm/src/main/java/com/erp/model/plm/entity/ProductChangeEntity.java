package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 产品变更信息表
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("product_change")
public class ProductChangeEntity extends BaseEntity<ProductChangeEntity> {

    /**
    * 变更单号
    */
    @TableField("code")
    private String code;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 作废状态（false未作废，true已作废）
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * skuid
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 变更原因
    */
    @TableField("reason")
    private String reason;
    /**
    * 变更日期
    */
    @TableField("bill_date")
    private LocalDate billDate;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 审核人id
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审核人姓名
    */
    @TableField("approve_user_name")
    private String approveUserName;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String REASON = "reason";

    public static final String BILL_DATE = "bill_date";

    public static final String PRODUCT_NAME = "product_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}