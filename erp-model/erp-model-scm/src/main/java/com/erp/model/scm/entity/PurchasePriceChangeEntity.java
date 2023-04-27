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

/**
 * <p>
 * 采购价变更表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_price_change")
public class PurchasePriceChangeEntity extends BaseEntity<PurchasePriceChangeEntity> {

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 变更原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;

    /**
     * 调价日期
     */
    @TableField("adjust_date")
    private LocalDate adjustDate;

    /**
     * 调价人id
     */
    @TableField("adjust_user_id")
    private String adjustUserId;

    /**
     * 定价人
     */
    @TableField("adjust_user_name")
    private String adjustUserName;

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
     * 采购价目表id
     *
     * @author yl
     * @date 2023-03-16 16:49
     * @param null
     * @return
     */
    @TableField("purchase_price_id")
    private String purchasePriceId;


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


    @Override
    public Serializable pkVal() {
        return null;
    }

}
