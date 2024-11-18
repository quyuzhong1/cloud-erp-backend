package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
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
@TableName("warehouse")
public class WarehouseEntity extends BaseEntity<WarehouseEntity> {

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
     * 负责人id
     */
    @TableField("charge_id")
    private String chargeId;



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
     * 是否禁用
     * true 禁用
     */
    @TableField("disabled")
    private Boolean disabled;

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
     * 金蝶仓库编号
     */
    @TableField("kingdee_warehouse_code")
    private String kingdeeWarehouseCode;

    /**
     * 是否虚拟仓
     * true 是
     */
    @TableField("is_virtual")
    private Boolean isVirtual;


    /**
     *
     * 仓库审核状态
     */
    @TableField(value = "approve_status")
    private ApproveStatusEnum approveStatus;

    /**
     * 是否启用仓位，true 启用 false 不启用
     */
    @TableField(value = "is_enable_location")
    private Boolean isEnableLocation;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 允许负库存
     */
    @TableField("allow_negative_inventory")
    private Boolean allowNegativeInventory;

    /**
     * 在途仓库id
     */
    @TableField("onway_warehouse_id")
    private String onwayWarehouseId;

    /**
     * 在途仓库名称
     */
    @TableField("onway_warehouse_name")
    private String onwayWarehouseName;

    /**
     * 经营类型
     */
    @TableField("warehouse_manage_type")
    private String warehouseManageType;

    /**
     * 地理位置
     */
    @TableField("geography_location")
    private String geographyLocation;

    /**
     * 排序
     */
    @TableField("index")
    private Integer index;

    @Override
    public Serializable pkVal() {
        return null;
    }

}
