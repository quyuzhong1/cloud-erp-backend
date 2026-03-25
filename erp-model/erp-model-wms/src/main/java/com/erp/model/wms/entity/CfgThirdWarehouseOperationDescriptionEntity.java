package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;


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
@TableName("cfg_third_warehouse_operation_description")
public class CfgThirdWarehouseOperationDescriptionEntity extends BaseEntity<CfgThirdWarehouseOperationDescriptionEntity> {

    /**
    * 三方仓编码
    */
    @TableField("third_warehouse_code")
    private String thirdWarehouseCode;
    /**
    * 三方仓名称
    */
    @TableField("third_warehouse_name")
    private String thirdWarehouseName;
    /**
    * 操作类型
    */
    @TableField("operation_type")
    private String operationType;
    /**
    * 输入类型:下拉框,输入框
    */
    @TableField("input_type")
    private String inputType;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 操作类型名称
    */
    @TableField("operation_type_name")
    private String operationTypeName;


    public static final String THIRD_WAREHOUSE_CODE = "third_warehouse_code";

    public static final String THIRD_WAREHOUSE_NAME = "third_warehouse_name";

    public static final String OPERATION_TYPE = "operation_type";

    public static final String INPUT_TYPE = "input_type";

    public static final String REMARK = "remark";

    public static final String OPERATION_TYPE_NAME = "operation_type_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}