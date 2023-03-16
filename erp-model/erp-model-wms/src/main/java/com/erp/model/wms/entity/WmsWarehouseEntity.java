package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 仓库表
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("wms_warehouse")
public class WmsWarehouseEntity extends BaseEntity<WmsWarehouseEntity> {

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 仓库类型 对应dict 表id
     */
    @TableField("type_id")
    private String typeId;

    /**
     * 类型名称
     */
    @TableField("type_name")
    private String typeName;

    /**
     * 负责人id
     */
    @TableField("charge_id")
    private String chargeId;

    /**
     * 负责人名
     */
    @TableField("charge_name")
    private String chargeName;

    /**
     * 联系人
     */
    @TableField("contacts")
    private String contacts;

    /**
     * 联系人电话
     */
    @TableField("contact_tel_number")
    private String contactTelNumber;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 地址 
     */
    @TableField("address")
    private String address;

    /**
     * 组织id 对应 核算公司表id
     */
    @TableField("org_id")
    private String orgId;

    /**
     * 组织名称
     */
    @TableField("org_name")
    private String orgName;




    @Override
    public Serializable pkVal() {
        return null;
    }

}
