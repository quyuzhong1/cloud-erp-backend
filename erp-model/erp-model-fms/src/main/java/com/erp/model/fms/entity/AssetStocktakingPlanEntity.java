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
 * 资产盘点方案表
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_stocktaking_plan")
public class AssetStocktakingPlanEntity extends BaseEntity<AssetStocktakingPlanEntity> {

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
    * 盘点方案编号
    */
    @TableField("code")
    private String code;
    /**
    * 盘点方案名称
    */
    @TableField("plan_name")
    private String planName;
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
    /**
    * 资产类别（机器设备）字符串数组
    */
    @TableField("asset_categories")
    private String assetCategories;
    /**
    * 使用部门ID字符串数组
    */
    @TableField("use_dept_ids")
    private String useDeptIds;
    /**
    * 资产位置ID字符串数组
    */
    @TableField("asset_location_ids")
    private String assetLocationIds;
    /**
    * 卡片编码-开始
    */
    @TableField("card_code_start")
    private String cardCodeStart;
    /**
    * 卡片编码-结束
    */
    @TableField("card_code_end")
    private String cardCodeEnd;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String INVALID_TIME = "invalid_time";

    public static final String CODE = "code";

    public static final String PLAN_NAME = "plan_name";

    public static final String ASSET_ORG_ID = "asset_org_id";

    public static final String ASSET_ORG_NAME = "asset_org_name";

    public static final String REMARK = "remark";

    public static final String ASSET_CATEGORIES = "asset_categories";

    public static final String USE_DEPT_IDS = "use_dept_ids";

    public static final String ASSET_LOCATION_IDS = "asset_location_ids";

    public static final String CARD_CODE_START = "card_code_start";

    public static final String CARD_CODE_END = "card_code_end";

    @Override
    public Serializable pkVal() {
        return null;
    }

}