package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 物理商表
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_logistics_supplier")
public class TransferLogisticsSupplierEntity extends BaseEntity<TransferLogisticsSupplierEntity> {

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
    * 是否禁用 true 禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 授权状态 授权状态 already 已授权 not未授权 cancel 取消授权
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

    public static final String FIELD_DISABLED = "disabled";

    public static final String AUTH_STATUS = "auth_status";

    public static final String AUTH_TIME = "auth_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}