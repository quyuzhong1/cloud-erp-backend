package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 中转报关产品
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare_product")
public class TransferDeclareProductEntity extends BaseEntity<TransferDeclareProductEntity> {

    /**
    * 销售订单明细id
    */
    @TableField("so_detail_id")
    private String soDetailId;
    /**
    * 来源id[中转报关单据]
    */
    @TableField("declare_id")
    private String declareId;
    /**
    * 来源明细id[中转报关单据]
    */
    @TableField("declare_detail_id")
    private String declareDetailId;
    /**
    * 产品sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 中文报关名称
    */
    @TableField("declare_chinese_name")
    private String declareChineseName;
    /**
    * 英文报关名称
    */
    @TableField("declare_english_name")
    private String declareEnglishName;
    /**
    * 出口报关申报价
    */
    @TableField("from_declare_price")
    private BigDecimal fromDeclarePrice;
    /**
    * 出口报关申报币种
    */
    @TableField("from_currency")
    private String fromCurrency;
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


    public static final String SO_DETAIL_ID = "so_detail_id";

    public static final String DECLARE_ID = "declare_id";

    public static final String DECLARE_DETAIL_ID = "declare_detail_id";

    public static final String SKU_NO = "sku_no";

    public static final String FIELD_QTY = "qty";

    public static final String DECLARE_CHINESE_NAME = "declare_chinese_name";

    public static final String DECLARE_ENGLISH_NAME = "declare_english_name";

    public static final String DECLARE_PRICE = "declare_price";

    public static final String CURRENCY = "currency";

    @Override
    public Serializable pkVal() {
        return null;
    }

}