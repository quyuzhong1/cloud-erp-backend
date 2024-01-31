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
 * b2b客户销售员变更单
 * </p>
 *
 * @author lrp
 * @since 2024-01-31
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("customer_b2b_seller_change")
public class CustomerB2bSellerChangeEntity extends BaseEntity<CustomerB2bSellerChangeEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 原销售id
    */
    @TableField("origin_seller_id")
    private String originSellerId;
    /**
    * 原销售名称
    */
    @TableField("origin_seller_name")
    private String originSellerName;
    /**
    * 变更后销售id
    */
    @TableField("change_seller_id")
    private String changeSellerId;
    /**
    * 变更后销售名
    */
    @TableField("change_seller_name")
    private String changeSellerName;
    /**
    * 开始日期
    */
    @TableField("start_date")
    private LocalDate startDate;
    /**
    * 审核人Id
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
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String ORIGIN_SELLER_ID = "origin_seller_id";

    public static final String ORIGIN_SELLER_NAME = "origin_seller_name";

    public static final String CHANGE_SELLER_ID = "change_seller_id";

    public static final String CHANGE_SELLER_NAME = "change_seller_name";

    public static final String START_DATE = "start_date";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}