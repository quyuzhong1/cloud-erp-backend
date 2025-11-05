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
 * 资产验收表
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_accept")
public class AssetAcceptEntity extends BaseEntity<AssetAcceptEntity> {

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
    * 验收单号
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
    * 验收日期
    */
    @TableField("accept_date")
    private LocalDate acceptDate;
    /**
    * 是否需要盖章
    */
    @TableField("is_need_seal")
    private Boolean isNeedSeal;
    /**
    * 验收组织ID
    */
    @TableField("accept_org_id")
    private String acceptOrgId;
    /**
    * 验收组织名称
    */
    @TableField("accept_org_name")
    private String acceptOrgName;
    /**
    * 验收人ID
    */
    @TableField("accept_user_id")
    private String acceptUserId;
    /**
    * 验收人姓名
    */
    @TableField("accept_user_name")
    private String acceptUserName;
    /**
    * 验收部门ID
    */
    @TableField("accept_dept_id")
    private String acceptDeptId;
    /**
    * 验收部门名称
    */
    @TableField("accept_dept_name")
    private String acceptDeptName;
    /**
    * 供应商ID
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 供应商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 验收说明
    */
    @TableField("accept_desc")
    private String acceptDesc;


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

    public static final String ACCEPT_DATE = "accept_date";

    public static final String IS_NEED_SEAL = "is_need_seal";

    public static final String ACCEPT_ORG_ID = "accept_org_id";

    public static final String ACCEPT_ORG_NAME = "accept_org_name";

    public static final String ACCEPT_USER_ID = "accept_user_id";

    public static final String ACCEPT_USER_NAME = "accept_user_name";

    public static final String ACCEPT_DEPT_ID = "accept_dept_id";

    public static final String ACCEPT_DEPT_NAME = "accept_dept_name";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String ACCEPT_DESC = "accept_desc";

    @Override
    public Serializable pkVal() {
        return null;
    }

}