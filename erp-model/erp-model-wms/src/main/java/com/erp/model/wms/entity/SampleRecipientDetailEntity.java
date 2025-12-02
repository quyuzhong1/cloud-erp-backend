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
 * 样品领用单明细
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_recipient_detail")
public class SampleRecipientDetailEntity extends BaseEntity<SampleRecipientDetailEntity> 
        implements SampleDocumentAuditUtil.SampleDocumentDetail {

    /**
    * 主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 执行状态 待出库/部分出库/已出库
    */
    @TableField("exec_status")
    private String execStatus;
    /**
    * SKU编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * SKU ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 领用数量
    */
    @TableField("recipient_qty")
    private Integer recipientQty;
    /**
    * 已出库数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String EXEC_STATUS = "exec_status";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String RECIPIENT_QTY = "recipient_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

    /**
     * 实现接口方法：获取数量
     * 领用单使用 recipientQty 字段
     */
    @Override
    public Integer getQty() {
        return this.recipientQty;
    }

    /**
     * 实现接口方法：获取样品台账ID
     * 领用单需要动态查询台账ID，此处返回null
     */
    @Override
    public String getSampleLedgerId() {
        return null;
    }

}