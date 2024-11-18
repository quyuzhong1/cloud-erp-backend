package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 仓库映射第三方平台表
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("warehouse_mapping")
public class WarehouseMappingEntity extends BaseEntity<WarehouseMappingEntity> {

    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 第三方平台仓库名称
    */
    @TableField("name")
    private String name;
    /**
    * 所属平台编码
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String WAREHOUSE_ID = "warehouse_id";

    

    public static final String DICT_PLATFORM = "dict_platform";

    

    @Override
    public Serializable pkVal() {
        return null;
    }

}