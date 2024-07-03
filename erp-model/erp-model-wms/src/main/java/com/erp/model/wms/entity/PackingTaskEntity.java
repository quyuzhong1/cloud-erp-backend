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
 * 装箱任务表
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("packing_task")
public class PackingTaskEntity extends BaseEntity<PackingTaskEntity> {

    /**
    * 装箱任务编码
    */
    @TableField("code")
    private String code;
    /**
    * 关联订单id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 关联订单编号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 单据类型(B2B,FBA,third)
     * PickingSourceTypeEnum
     *
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 发货仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 发货仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;


    public static final String CODE = "code";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}