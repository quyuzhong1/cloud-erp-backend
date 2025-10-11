package com.erp.model.fms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 资产盘点表
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_stocktaking")
public class AssetStocktakingEntity extends BaseEntity<AssetStocktakingEntity> {

    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    @TableField("approve_user_id")
    private String approveUserId;
    @TableField("approve_user_name")
    private String approveUserName;
    @TableField("approve_time")
    private LocalDateTime approveTime;
    @TableField("invalid_status")
    private Boolean invalidStatus;
    @TableField("invalid_remark")
    private String invalidRemark;
    @TableField("invalid_time")
    private LocalDateTime invalidTime;
    /**
    * 盘点单号
    */
    @TableField("code")
    private String code;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源ID
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 资产组织ID
    */
    @TableField("asset_org_id")
    private String assetOrgId;
    /**
    * 资产组织名称
    */
    @TableField("asset_org_name")
    private String assetOrgName;
    /**
    * 描述
    */
    @TableField("remark")
    private String remark;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String INVALID_TIME = "invalid_time";

    public static final String CODE = "code";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String ASSET_ORG_ID = "asset_org_id";

    public static final String ASSET_ORG_NAME = "asset_org_name";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}