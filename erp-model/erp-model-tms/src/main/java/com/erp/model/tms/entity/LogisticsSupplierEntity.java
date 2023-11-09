package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.erp.model.tms.enums.LogisticsSupplierTypeEnums;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 物理商表
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_supplier")
public class LogisticsSupplierEntity extends BaseEntity<LogisticsSupplierEntity> {

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 类型
    */
    @TableField("type")
    private LogisticsSupplierTypeEnums type;
    /**
    * 是否禁用 true 禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 授权状态
    */
    @TableField("auth_status")
    private String authStatus;
    /**
    * 授权时间
    */
    @TableField("auth_time")
    private LocalDateTime authTime;


    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String TYPE = "type";

    public static final String DISABLED = "disabled";

    public static final String AUTH_STATUS = "auth_status";

    public static final String AUTH_TIME = "auth_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}