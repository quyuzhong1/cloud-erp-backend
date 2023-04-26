package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

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
     * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
     */
    @TableField("sync_kingdee_status")
    private String syncKingdeeStatus;

    /**
     * 同步金蝶时间
     */
    @TableField("sync_kingdee_time")
    private LocalDateTime syncKingdeeTime;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;




    @Override
    public Serializable pkVal() {
        return null;
    }

}
