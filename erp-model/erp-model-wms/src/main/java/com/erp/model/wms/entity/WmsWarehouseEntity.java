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
    @TableField("contact_number")
    private String contactNumber;

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
    @TableField("organization_id")
    private String organizationId;

    /**
     * 组织名称
     */
    @TableField("organization_name")
    private String organizationName;


    public static final String NAME = "name";

    public static final String TYPE_ID = "type_id";

    public static final String TYPE_NAME = "type_name";

    public static final String CHARGE_ID = "charge_id";

    public static final String CHARGE_NAME = "charge_name";

    public static final String CONTACTS = "contacts";

    public static final String CONTACT_NUMBER = "contact_number";

    public static final String STATUS = "status";

    public static final String ADDRESS = "address";

    public static final String ORGANIZATION_ID = "organization_id";

    public static final String ORGANIZATION_NAME = "organization_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
