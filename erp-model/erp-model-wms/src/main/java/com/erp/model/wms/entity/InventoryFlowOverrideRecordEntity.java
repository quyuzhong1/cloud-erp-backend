package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.erp.model.wms.enums.InventoryFlowOverrideRecordTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 库存流水重算时间范围记录
 * </p>
 *
 * @author cloud
 * @since 2024-08-09
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("inventory_flow_override_record")
public class InventoryFlowOverrideRecordEntity extends BaseEntity<InventoryFlowOverrideRecordEntity> {

    /**
    * 开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;
    /**
    * 结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;
    /**
    * 库存组织id
    */
    @TableField("inventory_org_id")
    private String inventoryOrgId;
    /**
    * 库存组织名称
    */
    @TableField("inventory_org_name")
    private String inventoryOrgName;
    /**
    * 类型: auto=自动生成，manual=手动触发  枚举：InventoryFlowOverrideRecordTypeEnum
    */
    @TableField("type")
    private InventoryFlowOverrideRecordTypeEnum type;


    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String INVENTORY_ORG_ID = "inventory_org_id";

    public static final String INVENTORY_ORG_NAME = "inventory_org_name";

    public static final String INVENTORY_ORG_DESC = "inventory_org_desc";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
