package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 推送旺店通中间表Entity
 * @date 2024-07-24
 * @author tanmujin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_push_wdt")
public class DmpPushWdtEntity extends BaseEntity<DmpPushWdtEntity> {

    /**
     * erp单据id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * erp单据编码
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 推送给旺店通的单据编码（outer_no）
     */
    @TableField("third_code")
    private String thirdCode;

    /**
     * erp仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 第三方仓库编码
     */
    @TableField("third_warehouse_code")
    private String thirdWarehouseCode;

    /**
     * 推送到旺店通的单据类型：其他入库单/其他出库单
     */
    @TableField("third_type")
    private String thirdType;

    /**
     * 操作类型：审核/反审核
     */
    @TableField("operate_type")
    private String operateType;

    /**
     * 类型：0原始数据，1按仓位映射后数据
     */
    @TableField("type")
    private String type;
}
