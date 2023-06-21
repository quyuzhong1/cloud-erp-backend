package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.List;

import com.erp.model.dmp.kingdee.item.KingdeeTransferDirectItemEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 直接调拨单
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_transfer_info")
public class DmpTransferInfoEntity extends BaseEntity<DmpTransferInfoEntity> {


    /**
    * 单据编号
    */
    @TableField("code")
    private String code;

    /**
    * 单据类型
    */
    @TableField("type")
    private String type;

    /**
    * 调拨类型
    */
    @TableField("transfer_type")
    private String transferType;

    /**
    * 调拨类型编码
    */
    @TableField("transfer_type_code")
    private String transferTypeCode;

    /**
    * 调入组织id
    */
    @TableField("in_org_id")
    private String inOrgId;

    /**
    * 调入组织名称
    */
    @TableField("in_org_name")
    private String inOrgName;

    /**
    * 调出库存组织id
    */
    @TableField("out_org_id")
    private String outOrgId;

    /**
    * 调出库存组织名称
    */
    @TableField("out_org_name")
    private String outOrgName;

    /**
    * 单据日期
    */
    @TableField("bill_date")
    private LocalDateTime billDate;

    /**
    * 单据状态
    */
    @TableField("approve_status")
    private String approveStatus;

    /**
    * 调拨方向
    */
    @TableField("transfer_direction")
    private String transferDirection;

    /**
    * 平台创建人
    */
    @TableField("platform_create_user_name")
    private String platformCreateUserName;

    /**
    * 平台创建时间
    */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;

    /**
    * 审核人
    */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
    * 作废状态（false未作废，true已作废）
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
    * 作废时间
    */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;

    /**
    * 作废人
    */
    @TableField("invalid_user_name")
    private String invalidUserName;

    /**
    * 最后修改时间
    */
    @TableField("last_updated_time")
    private LocalDateTime lastUpdatedTime;

    /**
    * 最后修改人
    */
    @TableField("last_updated_user_name")
    private String lastUpdatedUserName;

    /**
    * 第三方单据id
    */
    @TableField("source_id")
    private String sourceId;

    /**
    * 来源平台 马帮，管易，金蝶等
    */
    @TableField("platform_sign")
    private String platformSign;

    /**
    * 同步马帮状态
    */
    @TableField("sync_mb_status")
    private String syncMbStatus;

    /**
    * 最新同步时间
    */
    @TableField("last_sync_mb_time")
    private LocalDateTime lastSyncMbTime;

    @TableField("remark")
    private String remark;
    /**
     * 调拨单明细
     */
    @TableField(exist = false)
    private List<DmpTransferInfoDetailEntity> itemList;


    public static final String CODE = "code";

    public static final String TYPE = "type";

    public static final String TRANSFER_TYPE = "transfer_type";

    public static final String TRANSFER_TYPE_CODE = "transfer_type_code";

    public static final String IN_ORG_ID = "in_org_id";

    public static final String IN_ORG_NAME = "in_org_name";

    public static final String OUT_ORG_ID = "out_org_id";

    public static final String OUT_ORG_NAME = "out_org_name";

    public static final String BILL_DATE = "bill_date";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String TRANSFER_DIRECTION = "transfer_direction";

    public static final String PLATFORM_CREATE_USER_NAME = "platform_create_user_name";

    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String INVALID_USER_NAME = "invalid_user_name";

    public static final String LAST_UPDATED_TIME = "last_updated_time";

    public static final String LAST_UPDATED_USER_NAME = "last_updated_user_name";

    public static final String SOURCE_ID = "source_id";

    public static final String PLAFORM_SIGN = "plaform_sign";

    public static final String SYNC_MB_STATUS = "sync_mb_status";

    public static final String LAST_SYNC_MB_TIME = "last_sync_mb_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}