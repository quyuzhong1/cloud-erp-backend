package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 采购价目表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_price")
public class PurchasePriceEntity extends BaseEntity<PurchasePriceEntity> {

    /**
     * 供应商表id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 审核状态 
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 报价日期
     */
    @TableField("quoted_date")
    private LocalDate quotedDate;

    /**
     * 报价人id
     */
    @TableField("pricing_user_id")
    private String pricingUserId;

    /**
     * 报价人
     */
    @TableField("pricing_user_name")
    private String pricingUserName;

    /**
     * 采购组织
     */
    @TableField("purchase_org_id")
    private String purchaseOrgId;

    /**
     * 采购组织名
     */
    @TableField("purchase_org_name")
    private String purchaseOrgName;

    /**
     * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
     */
    @TableField("sync_kingdee_status")
    private String syncKingdeeStatus;

    /**
     * 同步金蝶时间
     */
    @TableField("sync_kingdee_time")
    private LocalDateTime syncKingdeeTime;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 同步操作
     */
    @TableField("sync_operate")
    private String syncOperate;

    /**
     * 明细ids
     */
    @TableField(exist = false)
    private List<String> ids;


    @Override
    public Serializable pkVal() {
        return null;
    }

}
