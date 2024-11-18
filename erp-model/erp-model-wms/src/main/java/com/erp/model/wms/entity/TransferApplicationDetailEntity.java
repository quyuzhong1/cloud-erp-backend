package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 调拨申请单明细表
 *
 * @author will
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("transfer_application_detail")
public class TransferApplicationDetailEntity extends BaseEntity<TransferApplicationDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

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
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否自动生成加工单
     */
    @TableField("is_auto_machine")
    private Boolean isAutoMachine;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
