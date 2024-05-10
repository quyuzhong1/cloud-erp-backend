package com.erp.model.oms.entity;

import java.math.BigDecimal;
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
 * B2C销售订单申报产品信息表
 * </p>
 *
 * @author zdy
 * @since 2024-05-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_declare_product")
public class SoB2cDeclareProductEntity extends BaseEntity<SoB2cDeclareProductEntity> {

    /**
    * 销售订单明细id
    */
    @TableField("so_detail_id")
    private String soDetailId;
    /**
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 产品sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 申报数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 中文报关名称
    */
    @TableField("declare_cn")
    private String declareCn;
    /**
    * 英文报关名称
    */
    @TableField("declare_en")
    private String declareEn;
    /**
    * 目的国申报价
    */
    @TableField("to_declare_price")
    private BigDecimal toDeclarePrice;
    /**
    * 目的国申报币种
    */
    @TableField("to_currency")
    private String toCurrency;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 目的国申报币种符号
    */
    @TableField("to_currency_symbol")
    private String toCurrencySymbol;
    /**
    * 申报重量（取sku毛重）
    */
    @TableField("weight")
    private BigDecimal weight;
    /**
    * 目的国海关编码
    */
    @TableField("to_customs_code")
    private String toCustomsCode;

    /**
     * 申报标签(正常申报normal，高申报high，低申报low)
     */
    @TableField("declare_label")
    private String declareLabel;


    public static final String SO_DETAIL_ID = "so_detail_id";

    public static final String SO_ID = "so_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String DECLARE_CN = "declare_cn";

    public static final String DECLARE_EN = "declare_en";

    public static final String TO_DECLARE_PRICE = "to_declare_price";

    public static final String TO_CURRENCY = "to_currency";

    public static final String SKU_ID = "sku_id";

    public static final String TO_CURRENCY_SYMBOL = "to_currency_symbol";

    public static final String WEIGHT = "weight";

    public static final String TO_CUSTOMS_CODE = "to_customs_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}