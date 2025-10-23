package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.utils.SampleDocumentAuditUtil;


/**
 * <p>
 * 样品归还单明细表
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_return_detail")
public class SampleReturnDetailEntity extends BaseEntity<SampleReturnDetailEntity> 
        implements SampleDocumentAuditUtil.SampleDocumentDetail {

    /**
    * 关联归还单主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 来源明细ID
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
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
    * 归还数量
    */
    @TableField("return_qty")
    private Integer returnQty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 样品台账id
    */
    @TableField("sample_ledger_id")
    private String sampleLedgerId;


    public static final String MAIN_ID = "main_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String RETURN_QTY = "return_qty";

    public static final String REMARK = "remark";

    public static final String SAMPLE_LEDGER_ID = "sample_ledger_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

    /**
     * 实现接口方法：获取数量
     * 归还单使用 returnQty 字段
     */
    @Override
    public Integer getQty() {
        return this.returnQty;
    }

}