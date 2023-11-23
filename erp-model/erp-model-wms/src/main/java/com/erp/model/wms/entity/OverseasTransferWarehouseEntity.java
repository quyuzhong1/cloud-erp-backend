package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 海外仓签收记录
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("overseas_transfer_warehouse")
public class OverseasTransferWarehouseEntity extends BaseEntity<OverseasTransferWarehouseEntity> {

    /**
    * 平台类型
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 平台仓库代号
    */
    @TableField("platform_warehouse_code")
    private String platformWarehouseCode;
    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 平台海外仓状态
    */
    @TableField("platform_status")
    private String platformStatus;
    /**
     * 目的仓库ID
     */
    @TableField("dest_warehouse_id")
    private String destWarehouseId;
    /**
     * 目的仓库名称
     */
    @TableField("dest_warehouse_name")
    private String destWarehouseName;
    /**
     * 平台海外仓状态
     */
    @TableField("logistics_product_code")
    private String logisticsProductCode;

    /**
     * 物流产品名称
     */
    @TableField("logistics_product_name")
    private String logisticsProductName;

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String PLATFORM_WAREHOUSE_CODE = "platform_warehouse_code";

    public static final String NAME = "name";

    public static final String PLATFORM_STATUS = "platform_status";

}