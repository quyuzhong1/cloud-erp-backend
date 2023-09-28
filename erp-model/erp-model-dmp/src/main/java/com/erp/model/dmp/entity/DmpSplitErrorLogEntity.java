package com.erp.model.dmp.entity;

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
 * 
 * </p>
 *
 * @author Luo_WG
 * @since 2023-09-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_split_error_log")
public class DmpSplitErrorLogEntity extends BaseEntity<DmpSplitErrorLogEntity> {

    /**
    * bom表id
    */
    @TableField("bom_id")
    private String bomId;
    /**
    * 单据原sku
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 财务编码
    */
    @TableField("financial_code")
    private String financialCode;
    /**
    * 中台订单详情id
    */
    @TableField("item_id")
    private String itemId;
    /**
    * 错误描述
    */
    @TableField("msg")
    private String msg;


    public static final String BOM_ID = "bom_id";

    public static final String SKU_NO = "sku_no";

    public static final String FINANCIAL_CODE = "financial_code";

    public static final String ITEM_ID = "item_id";

    public static final String MSG = "msg";

    @Override
    public Serializable pkVal() {
        return null;
    }

}