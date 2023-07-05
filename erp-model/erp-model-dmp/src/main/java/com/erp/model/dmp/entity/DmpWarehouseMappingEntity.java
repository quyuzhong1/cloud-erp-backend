package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 平台仓库映射表
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_warehouse_mapping")
public class DmpWarehouseMappingEntity extends BaseEntity<DmpWarehouseMappingEntity> {


    /**
    * 仓库编码
    */
    @TableField("warehouse_code")
    private String warehouseCode;

    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
    * 仓库类型：自建仓库 PRIVATE 第三方仓库 THIRD_PARTY FBA仓 FBA
    */
    @TableField("type")
    private String type;

    /**
    * 是否默认仓库
    */
    @TableField("is_default")
    private Boolean isDefault;

    /**
    * 来源平台仓库id
    */
    @TableField("source_id")
    private String sourceId;

    /**
    * 来源平台名称
    */
    @TableField("platform_sign")
    private String platformSign;

    /**
    * 金蝶仓库编码
    */
    @TableField("kingdee_warehouse_code")
    private String kingdeeWarehouseCode;

    /**
    * 状态
    */
    @TableField("status")
    private String status;


    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String TYPE = "type";

    public static final String IS_DEFAULT = "is_default";

    public static final String SOURCE_ID = "source_id";

    public static final String PLATFORM_SIGN = "platform_sign";

    public static final String KINGDEE_WAREHOUSE_CODE = "kingdee_warehouse_code";

    public static final String STATUS = "status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}