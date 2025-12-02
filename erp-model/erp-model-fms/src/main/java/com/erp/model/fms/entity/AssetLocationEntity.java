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
 * 资产位置表
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_location")
public class AssetLocationEntity extends BaseEntity<AssetLocationEntity> {

    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 审批状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审批人ID
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审批人姓名
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 审批时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 是否作废
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 作废备注
    */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
    * 位置编码
    */
    @TableField("code")
    private String code;
    /**
    * 位置描述
    */
    @TableField("description")
    private String description;
    /**
    * 地址
    */
    @TableField("address")
    private String address;
    /**
    * 详细地址
    */
    @TableField("detailed_address")
    private String detailedAddress;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String DISABLED = "disabled";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String CODE = "code";

    public static final String DESCRIPTION = "description";

    public static final String ADDRESS = "address";

    public static final String DETAILED_ADDRESS = "detailed_address";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}