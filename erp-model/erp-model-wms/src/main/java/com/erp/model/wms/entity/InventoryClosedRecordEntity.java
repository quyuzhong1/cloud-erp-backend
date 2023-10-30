package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Map;

import com.erp.model.wms.dto.extension.TStkCloseProfileDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * <p>
 * 库存关账记录表
 * </p>
 *
 * @author Jim
 * @since 2023-10-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("inventory_closed_record")
public class InventoryClosedRecordEntity extends BaseEntity<InventoryClosedRecordEntity> {

    /**
    * 关账日期
    */
    @TableField("closed_date")
    private LocalDate closedDate;
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
    * 库存组织描述
    */
    @TableField("inventory_org_desc")
    private String inventoryOrgDesc;


    public static final String CLOSED_DATE = "closed_date";

    public static final String INVENTORY_ORG_ID = "inventory_org_id";

    public static final String INVENTORY_ORG_NAME = "inventory_org_name";

    public static final String INVENTORY_ORG_DESC = "inventory_org_desc";

    /**
     * 初始化
     */
    public static InventoryClosedRecordEntity init(TStkCloseProfileDTO dto, BaseIdDTO.CodeDTO codeDTO) {
        return new InventoryClosedRecordEntity()
                .setClosedDate(dto.getFCloseDate())
                .setInventoryOrgId(codeDTO.getId())
                .setInventoryOrgName(codeDTO.getName())
                .setInventoryOrgDesc(dto.getFCategory())
                ;
    }

    /**
     * 检查关账时间是否更新
     */
    public boolean isUpdateClosedDate(Map<String, LocalDate> newClosedDateMap) {
        LocalDate newClosedDate = newClosedDateMap.get(this.getInventoryOrgId());
        if (null == newClosedDate){
            return false;
        }
        return !this.closedDate.isEqual(newClosedDate);
    }

    /**
     * 更新关账时间
     */
    public InventoryClosedRecordEntity setClosedDateByMap(Map<String, LocalDate> newClosedDateMap) {
        LocalDate newClosedDate = newClosedDateMap.get(this.getInventoryOrgId());
        if (null != newClosedDate){
            this.setClosedDate(newClosedDate);
        }
        return this;
    }
}