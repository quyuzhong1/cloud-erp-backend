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


/**
 * <p>
 * 报关明细中间表
 * </p>
 *
 * @author jack
 * @since 2026-04-27
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
    * 报关状态：
    */
    @TableField("declare_status")
    private String declareStatus;
    /**
    * 生成状态：wait=未生成 finish=已生成
    */
    @TableField("generate_status")
    private String generateStatus;
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
    * 单据分类: firstMileDelivery=头程发货单, soDeliveryNotice=B2B发货通知单  枚举：SourceTypeEnum
    */
    @TableField("source_type")
    private String sourceType;

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
    /**
    * 发货仓ID
    */
    @TableField("from_warehouse_id")
    private String fromWarehouseId;
    /**
    * 发货仓名称
    */
    @TableField("from_warehouse_name")
    private String fromWarehouseName;
    /**
    * 目的仓ID
    */
    @TableField("dest_warehouse_id")
    private String destWarehouseId;
    /**
    * 目的仓名称
    */
    @TableField("dest_warehouse_name")
    private String destWarehouseName;
    /**
    * 中转仓IDs(逗号分隔)
    */
    @TableField("transfer_warehouse_ids")
    private String transferWarehouseIds;
    /**
    * 中转仓名称
    */
    @TableField("transfer_warehouse_names")
    private String transferWarehouseNames;
    /**
    * 销售组织ID
    */
    @TableField("sales_org_id")
    private String salesOrgId;
    /**
    * 销售组织名称
    */
    @TableField("sales_org_name")
    private String salesOrgName;
    /**
    * bom历史记录id（表product_bom_history）
    */
    @TableField("bom_history_id")
    private String bomHistoryId;
    /**
    * bom版本（表product_bom_info）
    */
    @TableField("bom_version")
    private String bomVersion;


    public static final String REMARK = "remark";

    public static final String DECLARE_STATUS = "declare_status";

    public static final String GENERATE_STATUS = "generate_status";

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

    public static final String FROM_WAREHOUSE_ID = "from_warehouse_id";

    public static final String FROM_WAREHOUSE_NAME = "from_warehouse_name";

    public static final String DEST_WAREHOUSE_ID = "dest_warehouse_id";

    public static final String DEST_WAREHOUSE_NAME = "dest_warehouse_name";

    public static final String TRANSFER_WAREHOUSE_IDS = "transfer_warehouse_ids";

    public static final String TRANSFER_WAREHOUSE_NAMES = "transfer_warehouse_names";

    public static final String SALES_ORG_ID = "sales_org_id";

    public static final String SALES_ORG_NAME = "sales_org_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
