package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 加工单明细
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("machine_detail")
public class MachineDetailEntity extends BaseEntity<MachineDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 参照版本
     */
    @TableField("reference_version")
    private String referenceVersion;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * bom历史版本id
     */
    @TableField("bom_history_id")
    private String bomHistoryId;

    @TableField(exist = false)
    private List<MachineSubComponentsDTO.UpdateDTO> subComponentsList;

    @TableField(exist = false)
    private Integer index;

    /**
     * 关联id，用于销售单下推
     */
    @TableField(exist = false)
    private String refId;

    /**
     * 关联编码，用于销售单下推
     */
    @TableField(exist = false)
    private String refCode;

    /**
     * 关联明细id，用于销售单下推
     */
    @TableField(exist = false)
    private String refDetailId;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String CUR_INVENTORY_QTY = "cur_inventory_qty";

    public static final String REFERENCE_VERSION = "reference_version";


    @Override
    public Serializable pkVal() {
        return null;
    }

}
