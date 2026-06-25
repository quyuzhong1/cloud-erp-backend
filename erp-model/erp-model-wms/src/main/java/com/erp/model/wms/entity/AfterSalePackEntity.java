package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 售后装箱表
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("after_sale_pack")
public class AfterSalePackEntity extends BaseEntity<AfterSalePackEntity> {

    /**
     * 箱唛
     */
    @TableField("code")
    private String code;
    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;
    /**
     * 来源单号
     */
    @TableField("source_code")
    private String sourceCode;
    /**
     * 来源单据类型
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 箱唛类型
     */
    @TableField("type")
    private String type;
    /**
     * 是否被单据使用 false 未使用 true 已使用
     */
    @TableField("is_use")
    private Boolean isUse;
    /**
     * 箱唛状态
     */
    @TableField("pack_status")
    private String packStatus;
    /**
     * 是否存在差异 false 否 true 是
     */
    @TableField("is_difference")
    private Boolean isDifference;
    /**
     * 是否移仓 false 否 true 是
     */
    @TableField("is_move_warehouse")
    private Boolean isMoveWarehouse;
    /**
     * sku种类数
     */
    @TableField("sku_species_qty")
    private Integer skuSpeciesQty;
    /**
     * 总数量
     */
    @TableField("total_qty")
    private Integer totalQty;
    /**
     * 驳回原因
     */
    @TableField("reject_description")
    private String rejectDescription;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String CODE = "code";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String TYPE = "type";

    public static final String IS_USE = "is_use";

    public static final String PACK_STATUS = "pack_status";

    public static final String IS_DIFFERENCE = "is_difference";

    public static final String IS_MOVE_WAREHOUSE = "is_move_warehouse";

    public static final String SKU_SPECIES_QTY = "sku_species_qty";

    public static final String TOTAL_QTY = "total_qty";

    public static final String REJECT_DESCRIPTION = "reject_description";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}