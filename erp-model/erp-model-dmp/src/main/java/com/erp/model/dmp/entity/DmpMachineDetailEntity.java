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
 * 加工单明细
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_machine_detail")
public class DmpMachineDetailEntity extends BaseEntity<DmpMachineDetailEntity> {


    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;

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
    private Integer referenceVersion;

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
    * 来源详情id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String UNIT = "unit";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String REFERENCE_VERSION = "reference_version";

    public static final String REMARK = "remark";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}