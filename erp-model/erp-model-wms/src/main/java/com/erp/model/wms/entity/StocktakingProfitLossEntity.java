package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.BillTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 盘盈盘亏单
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("stocktaking_profit_loss")
public class StocktakingProfitLossEntity extends BaseEntity<StocktakingProfitLossEntity> {

    /**
     * 单号
     */
    @TableField("code")
    private String code;

    /**
     * 来源id 盘点任务id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源code 盘点任务code
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 单据类型
     */
    @TableField("bill_type")
    private BillTypeEnum billType;

    /**
     * 单据日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

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
     * 审核人名
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 审核时间
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;


    /**
     * 库存组织
     */
    @TableField("inventory_org_id")
    private String inventoryOrgId;

    /**
     * 库存组织名
     */
    @TableField("inventory_org_name")
    private String inventoryOrgName;



    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String BILL_TYPE = "bill_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
