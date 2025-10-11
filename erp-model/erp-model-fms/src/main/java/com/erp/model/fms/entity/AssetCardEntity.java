package com.erp.model.fms.entity;

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
 * 资产卡片主表
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_card")
public class AssetCardEntity extends BaseEntity<AssetCardEntity> {

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
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 卡片来源（采购收货 手工建卡 盘盈建卡）
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
    @TableField("org_id")
    private String orgId;
    /**
    * 资产组织名称
    */
    @TableField("org_name")
    private String orgName;
    /**
    * 资产类型（机器设备）
    */
    @TableField("type")
    private String type;
    /**
    * 卡片编码
    */
    @TableField("code")
    private String code;
    /**
    * 资产状态（正常使用）
    */
    @TableField("status")
    private String status;
    /**
    * 变动方式（购入 盘盈）
    */
    @TableField("change_method")
    private String changeMethod;
    /**
    * 资产名称
    */
    @TableField("name")
    private String name;
    /**
    * 计量单位
    */
    @TableField("unit")
    private String unit;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 开始使用日期
    */
    @TableField("start_use_date")
    private LocalDate startUseDate;
    /**
    * 资产编码
    */
    @TableField("asset_code")
    private String assetCode;
    /**
    * 资产位置ID
    */
    @TableField("asset_location_id")
    private String assetLocationId;
    /**
    * 处置情况（空 部分处置 完全清理）
    */
    @TableField("disposal_status")
    private String disposalStatus;
    /**
    * 备注
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

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String ORG_ID = "org_id";

    public static final String ORG_NAME = "org_name";

    public static final String TYPE = "type";

    public static final String CODE = "code";

    public static final String STATUS = "status";

    public static final String CHANGE_METHOD = "change_method";

    public static final String NAME = "name";

    public static final String UNIT = "unit";

    public static final String QTY = "qty";

    public static final String START_USE_DATE = "start_use_date";

    public static final String ASSET_CODE = "asset_code";

    public static final String ASSET_LOCATION_ID = "asset_location_id";

    public static final String DISPOSAL_STATUS = "disposal_status";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}