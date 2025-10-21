package com.erp.model.plm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
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
 * @author wtr
 * @since 2025-10-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_notice_detail")
public class AssetNoticeDetailEntity extends BaseEntity<AssetNoticeDetailEntity> {

    /**
    * 资产通知单头id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 资产id
    */
    @TableField("asset_id")
    private String assetId;
    /**
    * 资产编码
    */
    @TableField("asset_code")
    private String assetCode;
    /**
    * 资产名称
    */
    @TableField("asset_name")
    private String assetName;
    /**
    * 标识(首套模、复制模)
    */
    @TableField("tag")
    private String tag;
    /**
    * 是否加急
    */
    @TableField("is_urgent")
    private Boolean isUrgent;
    /**
    * 计划交期
    */
    @TableField("plan_deliver_date")
    private LocalDate planDeliverDate;
    /**
    * 申请数量
    */
    @TableField("apply_qty")
    private BigDecimal applyQty;
    /**
    * 采购组织id
    */
    @TableField("purchase_org_id")
    private String purchaseOrgId;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
     * 采购订单生成状态（0未生成，1部分生成，2已生成）
     */
    @TableField("create_po_type")
    private String createPoType;


    public static final String MAIN_ID = "main_id";

    public static final String ASSET_ID = "asset_id";

    public static final String ASSET_CODE = "asset_code";

    public static final String ASSET_NAME = "asset_name";

    public static final String TAG = "tag";

    public static final String IS_URGENT = "is_urgent";

    public static final String PLAN_DELIVER_DATE = "plan_deliver_date";

    public static final String APPLY_QTY = "apply_qty";

    public static final String PURCHASE_ORG_ID = "purchase_org_id";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}