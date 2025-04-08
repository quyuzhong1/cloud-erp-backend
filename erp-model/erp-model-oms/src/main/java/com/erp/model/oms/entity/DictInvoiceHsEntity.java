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
 * 发票海关编码
 * </p>
 *
 * @author will
 * @since 2025-04-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_invoice_hs")
public class DictInvoiceHsEntity extends BaseEntity<DictInvoiceHsEntity> {

    /**
    * 海关编码
    */
    @TableField("hs_code")
    private String hsCode;
    /**
    * 描述
    */
    @TableField("desc")
    private String desc;
    /**
    * 国家
    */
    @TableField("country")
    private String country;


    public static final String HS_CODE = "hs_code";

    public static final String DESC = "desc";

    public static final String COUNTRY = "country";

    @Override
    public Serializable pkVal() {
        return null;
    }

}