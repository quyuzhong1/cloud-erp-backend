package com.erp.model.oms.entity;

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
 * 发票更新历史
 * </p>
 *
 * @author will
 * @since 2025-04-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("invoice_update_his")
public class InvoiceUpdateHisEntity extends BaseEntity<InvoiceUpdateHisEntity> {

    /**
    * 发票清单id
    */
    @TableField("invoice_info_id")
    private String invoiceInfoId;
    /**
    * 更新内容
    */
    @TableField("content")
    private String content;


    public static final String INVOICE_INFO_ID = "invoice_info_id";

    public static final String CONTENT = "content";

    @Override
    public Serializable pkVal() {
        return null;
    }

}