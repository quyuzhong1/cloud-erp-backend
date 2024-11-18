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
     * 字典地址 http://172.16.100.11:3002/project/92/interface/api/13147  type = packingSourceType
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

    /**
     * 装箱状态 wait:未生成;unpacked:待装箱;packing:装箱中;packed:已装箱
     * PackingTaskStatusEnum
     * 字典接口地址  http://172.16.100.11:3002/project/92/interface/api/13147  type = packingStatusSingle 单箱 / packingStatus总
     */
    @TableField("packing_status")
    private String packingStatus;
    /**
     * 称重状态-总箱(unweighed 未称重,weighing 部分称重, weighed 全部称重 )
     * PackingWeightStatusEnum
     * 字典接口地址  http://172.16.100.11:3002/project/92/interface/api/13147 type= weightingStatusSingle单箱 /  weightingStatus 总
     */
    @TableField("weighting_status")
    private String weightingStatus;

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