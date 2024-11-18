package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 海外物流商仓库
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("overseas_provider_warehouse")
public class OverseasProviderWarehouseEntity extends BaseEntity<OverseasProviderWarehouseEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 仓库编码
    */
    @TableField("platform_warehouse_code")
    private String platformWarehouseCode;
    /**
    * 仓库名称
    */
    @TableField("platform_warehouse_name")
    private String platformWarehouseName;
    /**
    * 国家二字码
    */
    @TableField("country")
    private String country;
    /**
    * 国家中文名
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 系统仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 系统仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 系统仓库编码
    */
    @TableField("warehouse_code")
    private String warehouseCode;
    /**
    * 是否禁用 true 禁用 false 启用
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String MAIN_ID = "main_id";

    public static final String PLATFORM_WAREHOUSE_CODE = "platform_warehouse_code";

    public static final String PLATFORM_WAREHOUSE_NAME = "platform_warehouse_name";

    public static final String COUNTRY_NAME = "country_name";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_CODE = "warehouse_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}