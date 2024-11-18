package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 数据对比映射方案
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wms_data_compare_plan")
public class WmsDataComparePlanEntity extends BaseEntity<WmsDataComparePlanEntity> {

    /**
    * 映射名称
    */
    @TableField("name")
    private String name;
    /**
    * 单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收  枚举：WmsDataComparePlanBillTypeEnum
    */
    @TableField("bill_type")
    private String billType;
    /**
    * 导入数据字段映射json串
    */
    @TableField("import_data_mapping")
    private String importDataMapping;


    

    public static final String BILL_TYPE = "bill_type";

    public static final String IMPORT_DATA_MAPPING = "import_data_mapping";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
