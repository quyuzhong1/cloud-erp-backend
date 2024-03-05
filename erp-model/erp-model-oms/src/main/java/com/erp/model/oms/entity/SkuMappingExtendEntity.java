package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;

import com.erp.model.wms.enums.WarehouseDeliveryTypeEnum;
import com.erp.model.wms.enums.WarehouseManageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * <p>
 * sku仓库发货配置
 * </p>
 *
 * @author Jim
 * @since 2024-02-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sku_mapping_extend")
public class SkuMappingExtendEntity extends BaseEntity<SkuMappingExtendEntity> {

    /**
     * sku_mapping表主键id
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 仓库经营类型:selfBuild=自建,thirdParty=第三方仓库
     * {@link WarehouseManageTypeEnum}
     */
    @TableField("warehouse_manage_type")
    private String warehouseManageType;
    /**
     * 发货类型:single=子件发货,combine=捆绑Sku发货
     * {@link WarehouseDeliveryTypeEnum}
     */
    @TableField("delivery_type")
    private String deliveryType;


    public static final String MAIN_ID = "main_id";

    public static final String MANAGEMENT = "management";

    public static final String DELIVERY_TYPE = "delivery_type";
}