package com.erp.model.tms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 报关明细中间表
 * </p>
 *
 * @author jack
 * @since 2026-04-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("delivery_declare_detail_mid")
public class DeliveryDeclareDetailMidEntity extends BaseEntity<DeliveryDeclareDetailMidEntity> {

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 报关状态 DeclareStatusEnum
    */
    @TableField("declare_status")
    private String declareStatus;
    /**
    * 来源单据id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 单据分类: firstMileDelivery=头程发货单, soDeliveryNotice=B2B发货通知单
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 业务单据id
    */
    @TableField("business_id")
    private String businessId;
    /**
    * 业务单号
    */
    @TableField("business_code")
    private String businessCode;
    /**
    * 业务单据类型
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 合同协议号
    */
    @TableField("contract_no")
    private String contractNo;
    /**
    * 商品id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 商品SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 币种符号
    */
    @TableField("currency_symbol")
    private String currencySymbol;
    /**
    * 报关单主表id
    */
    @TableField("declare_id")
    private String declareId;
    /**
    * 报关单号
    */
    @TableField("declare_code")
    private String declareCode;
    /**
    * 报关单明细id
    */
    @TableField("declare_detail_id")
    private String declareDetailId;
    /**
    * 箱号
    */
    @TableField("box_no")
    private String boxNo;
    /**
    * 中国海关编码
    */
    @TableField("hs_code")
    private String hsCode;
    /**
    * 报关中文名称
    */
    @TableField("product_name_cn")
    private String productNameCn;
    /**
    * 申报要素
    */
    @TableField("declare_element")
    private String declareElement;
    /**
    * 单位
    */
    @TableField("unit")
    private String unit;
    /**
    * 出口申报单价
    */
    @TableField("unit_price")
    private BigDecimal unitPrice;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;


    public static final String REMARK = "remark";

    public static final String DECLARE_STATUS = "declare_status";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String BUSINESS_ID = "business_id";

    public static final String BUSINESS_CODE = "business_code";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String CONTRACT_NO = "contract_no";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String DECLARE_ID = "declare_id";

    public static final String DECLARE_CODE = "declare_code";

    public static final String DECLARE_DETAIL_ID = "declare_detail_id";

    public static final String BOX_NO = "box_no";

    public static final String HS_CODE = "hs_code";

    public static final String PRODUCT_NAME_CN = "product_name_cn";

    public static final String DECLARE_ELEMENT = "declare_element";

    public static final String UNIT = "unit";

    public static final String UNIT_PRICE = "unit_price";

    public static final String QTY = "qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}