package com.erp.model.dmp.entity;

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
 * 中台销售订单出库详情明细表
 * </p>
 *
 * @author shukai
 * @since 2024-06-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_outstock_detail")
public class DmpSoOutstockDetailEntity extends BaseEntity<DmpSoOutstockDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 来源详情id
    */
    @TableField("third_detail_id")
    private String thirdDetailId = "";
    /**
    * 销售平台原始详情id
    */
    @TableField("platform_detail_id")
    private String platformDetailId = "";
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId = "";
    /**
    * sku编码
    */
    @TableField("sku_no")
    private String skuNo = "";
    /**
    * 产品名称
    */
    @TableField("sku_name")
    private String skuName = "";
    /**
    * 平台sku
    */
    @TableField("platform_sku")
    private String platformSku = "";
    /**
    * 单位
    */
    @TableField("product_unit")
    private String productUnit = "";
    /**
    * 是否赠品：true/false
    */
    @TableField("is_gift")
    private Boolean isGift;
    /**
    * 商品规格
    */
    @TableField("specifics")
    private String specifics = "";
    /**
    * 商品备注
    */
    @TableField("item_remark")
    private String itemRemark = "";
    /**
    * 仓库编号
    */
    @TableField("warehouse_id")
    private String warehouseId = "";
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName = "";
    /**
    * 商品仓位
    */
    @TableField("warehouse_location")
    private String warehouseLocation = "";
    /**
    * 商品单价
    */
    @TableField("sell_price")
    private BigDecimal sellPrice;
    /**
    * 商品数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 金额
    */
    @TableField("amount")
    private BigDecimal amount;
    /**
    * 第三方平台订单编号
    */
    @TableField("third_order_code")
    private String thirdOrderCode = "";
    /**
    * 销售平台原始订单编号
    */
    @TableField("platform_order_code")
    private String platformOrderCode = "";
    /**
     * 税率
     */
     @TableField("tax_rate")
     private BigDecimal taxRate;
    /**
     * 来源订单明细id
     */
     @TableField("src_order_detail_id")
     private String srcOrderDetailId = "";
     /**
      * 客户名称
      */
     @TableField("customer_name")
     private String customerName = "";
     /**
      * 平台名称
      */
     @TableField("platform_name")
     private String platformName = "";
     
     /**
      * 销售部门名称
      */
     @TableField("sale_dept_name")
     private String saleDeptName = "";
     
     /**
      * 销售员名称
      */
     @TableField("sales_man_name")
     private String salesManName = "";
    /**
     * 备注
     */
     @TableField("remark")
     private String remark = "";
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;


    public static final String MAIN_ID = "main_id";

    public static final String THIRD_DETAIL_ID = "third_detail_id";

    public static final String PLATFORM_DETAIL_ID = "platform_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_NAME = "sku_name";

    public static final String PLATFORM_SKU = "platform_sku";

    public static final String PRODUCT_UNIT = "product_unit";

    public static final String IS_GIFT = "is_gift";

    public static final String SPECIFICS = "specifics";

    public static final String ITEM_REMARK = "item_remark";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String SELL_PRICE = "sell_price";

    public static final String QTY = "qty";

    public static final String AMOUNT = "amount";

    public static final String THIRD_ORDER_CODE = "third_order_code";

    public static final String PLATFORM_ORDER_CODE = "platform_order_code";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    @Override
    public Serializable pkVal() {
        return null;
    }

}