package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 中台销售退货订单明细表
 * </p>
 *
 * @author shukai
 * @since 2024-06-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_return_detail")
public class DmpSoReturnDetailEntity extends BaseEntity<DmpSoReturnDetailEntity> {

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
    * 退货原因
    */
    @TableField("reason")
    private String reason = "";
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
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 商品售价
    */
    @TableField("sell_price")
    private BigDecimal sellPrice;
    /**
    * 金额
    */
    @TableField("amount")
    private BigDecimal amount;
    /**
    * 是否赠品：true/false
    */
    @TableField("is_gift")
    private Boolean isGift;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId = "";
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName = "";
    /**
    * 仓位
    */
    @TableField("warehouse_location")
    private String warehouseLocation = "";
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
     * so明细id
     */
     @TableField("so_entry_id")
     private String soEntryId = "";
     
    /**
     * 明细状态
     */
	@TableField("detail_status")
	private String detailStatus = "";
	
	/**
	 * 退货物流公司
	 */
	@TableField("return_logistics_company")
	private String returnLogisticsCompany = "";
	
	/**
	 * 退货物流单号
	 */
	@TableField("return_logistics_no")
	private String returnLogisticsNo = "";
	
	/**
	 * 退货运费金额
	 */
	@TableField("logistics_fee_amount")
	private BigDecimal logisticsFeeAmount;
	
	/**
	 * 退货运费币种
	 */
	@TableField("logistics_fee_currency")
	private String logisticsFeeCurrency = "";
	
	/**
	 * 退货运费承担方
	 */
	@TableField("logistics_fee_role")
	private String logisticsFeeRole = "";
	
	/**
	 * 币别
	 */
	@TableField("currency")
	private String currency = "";
	
	/**
	 * 方案类型：退款refund、退货退款return_and_refund
	 */
	@TableField("solution_type")
	private String solutionType = "";
	
	/**
	 * 是否收到货：true/false
	*/
    @TableField("receive_goods")
    private Boolean receiveGoods;
    
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

    /**
     * 退款税费
     */
    @TableField("refund_tax")
    private BigDecimal refundTax;

    public static final String MAIN_ID = "main_id";

    public static final String THIRD_DETAIL_ID = "third_detail_id";

    public static final String PLATFORM_DETAIL_ID = "platform_detail_id";

    public static final String REASON = "reason";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String SELL_PRICE0 = "sell_price0";

    public static final String AMOUNT = "amount";

    public static final String IS_GIFT = "is_gift";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

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