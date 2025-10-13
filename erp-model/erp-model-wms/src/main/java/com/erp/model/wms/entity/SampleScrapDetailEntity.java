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
 * 样品报废单明细表
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_scrap_detail")
public class SampleScrapDetailEntity extends BaseEntity<SampleScrapDetailEntity> {

    /**
    * 关联报废单主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * SKU ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * SKU编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 报废数量
    */
    @TableField("scrap_qty")
    private Integer scrapQty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 台账id
    */
    @TableField("sample_ledger_id")
    private String sampleLedgerId;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String SCRAP_QTY = "scrap_qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}