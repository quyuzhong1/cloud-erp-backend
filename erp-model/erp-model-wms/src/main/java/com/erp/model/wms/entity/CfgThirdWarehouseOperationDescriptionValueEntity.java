package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 
 * </p>
 *
 * @author wtr
 * @since 2026-03-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_third_warehouse_operation_description_value")
public class CfgThirdWarehouseOperationDescriptionValueEntity extends BaseEntity<CfgThirdWarehouseOperationDescriptionValueEntity> {

    /**
    * cfg_third_warehouse_operation_description表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 下拉值名称
    */
    @TableField("name")
    private String name;
    /**
    * 下拉值
    */
    @TableField("value")
    private String value;


    public static final String MAIN_ID = "main_id";

    public static final String NAME = "name";

    public static final String VALUE = "value";

    @Override
    public Serializable pkVal() {
        return null;
    }

}