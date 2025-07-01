package com.erp.model.scm.entity;

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
 * 合同管理表
 * </p>
 *
 * @author will
 * @since 2025-06-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("contract_info")
public class ContractInfoEntity extends BaseEntity<ContractInfoEntity> {

    /**
    * 审核状态 
    */
    @TableField("approve_status")
    private String approveStatus;
    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 服务商id
    */
    @TableField("service_provider_id")
    private String serviceProviderId;
    /**
    * 服务商名称
    */
    @TableField("service_provider_name")
    private String serviceProviderName;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDate approveTime;
    /**
    * 审核人
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 合同类型,contractType字典
    */
    @TableField("type")
    private String type;
    /**
    * 生效时间
    */
    @TableField("effective_date")
    private LocalDate effectiveDate;
    /**
    * 失效时间
    */
    @TableField("expire_date")
    private LocalDate expireDate;
    /**
     * ContractInfoStatusEnum 生效状态：notEffective=未生效,effective=生效中,expired=失效
     */
    @TableField("status")
    private String status;
    /**
    * 是否禁用
    */
    @TableField("disable")
    private Boolean disable;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String SERVICE_PROVIDER_ID = "service_provider_id";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String TYPE = "type";

    public static final String EFFECTIVE_DATE = "effective_date";

    public static final String EXPIRE_DATE = "expire_date";

    public static final String DISABLE = "disable";

    @Override
    public Serializable pkVal() {
        return null;
    }

}