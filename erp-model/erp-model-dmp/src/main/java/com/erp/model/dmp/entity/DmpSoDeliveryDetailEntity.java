package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 中台配货单明细表
 * </p>
 *
 * @author shukai
 * @since 2025-04-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_delivery_detail")
public class DmpSoDeliveryDetailEntity extends BaseEntity<DmpSoDeliveryDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 第三方发货id
    */
    @TableField("third_delivery_id")
    private String thirdDeliveryId;
    /**
    * 第三方发货明细id
    */
    @TableField("third_delivery_detail_id")
    private String thirdDeliveryDetailId;
    /**
    * 第三方明细创建时间
    */
    @TableField("third_detail_create_time")
    private LocalDateTime thirdDetailCreateTime;
    /**
    * 第三方明细更新时间
    */
    @TableField("third_detail_update_time")
    private LocalDateTime thirdDetailUpdateTime;
    /**
    * 商家SKU编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 商家SKU名称
    */
    @TableField("sku_name")
    private String skuName;
    /**
    * 成交数量
    */
    @TableField("transaction_qty")
    private Integer transactionQty;
    /**
    * 成交单价
    */
    @TableField("transaction_price")
    private BigDecimal transactionPrice;
    /**
    * 单位
    */
    @TableField("unit")
    private String unit;
    /**
    * 成交金额
    */
    @TableField("transaction_amount")
    private BigDecimal transactionAmount;
    /**
     * 折扣金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;
    /**
     * 运费金额
     */
    @TableField("freight_amount")
    private BigDecimal freightAmount;
    /**
     * 税费金额
     */
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    /**
    * 规格型号
    */
    @TableField("spu_no")
    private String spuNo;
    /**
    * 规格型号名称
    */
    @TableField("spu_name")
    private String spuName;
    /**
    * 是否赠品
    */
    @TableField("is_gift")
    private Integer isGift;
    /**
    * 是否组合装
    */
    @TableField("is_comb")
    private Integer isComb;
    /**
    * 是否虚拟商品
    */
    @TableField("is_virtual")
    private Integer isVirtual;
    /**
    * 是否服务类商品
    */
    @TableField("is_service")
    private Integer isService;
    /**
    * 明细状态
    */
    @TableField("detail_status")
    private String detailStatus;
    /**
    * 基准售价
    */
    @TableField("list_price")
    private BigDecimal listPrice;
    /**
    * 交易币别代码
    */
    @TableField("currency_code")
    private String currencyCode;
    /**
    * 交易币别名称
    */
    @TableField("currency_name")
    private String currencyName;
    /**
    * 组合装编码
    */
    @TableField("suite_no")
    private String suiteNo;
    /**
    * 组合装名称
    */
    @TableField("suite_name")
    private String suiteName;
    /**
    * MSKU编码
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * MSKU名称
    */
    @TableField("platform_sku_name")
    private String platformSkuName;
    /**
    * 结算币别代码
    */
    @TableField("settlement_currency_code")
    private String settlementCurrencyCode;
    /**
    * 国家编码
    */
    @TableField("country_code")
    private String countryCode;
    /**
    * 国家名称
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 区域编码
    */
    @TableField("region_code")
    private String regionCode;
    /**
    * 区域名称
    */
    @TableField("region_name")
    private String regionName;
    /**
    * 军区编码
    */
    @TableField("military_region_code")
    private String militaryRegionCode;
    /**
    * 军区名称
    */
    @TableField("military_region_name")
    private String militaryRegionName;
    /**
    * 部门编码
    */
    @TableField("department_code")
    private String departmentCode;
    /**
    * 部门名称
    */
    @TableField("department_name")
    private String departmentName;
    /**
    * 数据状态(已创建，已更新，已删除等)
    */
    @TableField("data_status")
    private String dataStatus;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 销售订单id
     */
    @TableField("so_id")
    private String soId;
    /**
     * 销售订单明细id
     */
    @TableField("so_detail_id")
    private String soDetailId;
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


    public static final String MAIN_ID = "main_id";

    public static final String THIRD_DELIVERY_ID = "third_delivery_id";

    public static final String THIRD_DELIVERY_DETAIL_ID = "third_delivery_detail_id";

    public static final String THIRD_DETAIL_CREATE_TIME = "third_detail_create_time";

    public static final String THIRD_DETAIL_UPDATE_TIME = "third_detail_update_time";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_NAME = "sku_name";

    public static final String TRANSACTION_QTY = "transaction_qty";

    public static final String TRANSACTION_PRICE = "transaction_price";

    public static final String UNIT = "unit";

    public static final String TRANSACTION_AMOUNT = "transaction_amount";

    public static final String SPU_NO = "spu_no";

    public static final String SPU_NAME = "spu_name";

    public static final String IS_GIFT = "is_gift";

    public static final String IS_COMB = "is_comb";

    public static final String IS_VIRTUAL = "is_virtual";

    public static final String IS_SERVICE = "is_service";

    public static final String DETAIL_STATUS = "detail_status";

    public static final String LIST_PRICE = "list_price";

    public static final String CURRENCY_CODE = "currency_code";

    public static final String CURRENCY_NAME = "currency_name";

    public static final String SUITE_NO = "suite_no";

    public static final String SUITE_NAME = "suite_name";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String PLATFORM_SKU_NAME = "platform_sku_name";

    public static final String SETTLEMENT_CURRENCY_CODE = "settlement_currency_code";

    public static final String COUNTRY_CODE = "country_code";

    public static final String COUNTRY_NAME = "country_name";

    public static final String REGION_CODE = "region_code";

    public static final String REGION_NAME = "region_name";

    public static final String MILITARY_REGION_CODE = "military_region_code";

    public static final String MILITARY_REGION_NAME = "military_region_name";

    public static final String DEPARTMENT_CODE = "department_code";

    public static final String DEPARTMENT_NAME = "department_name";

    public static final String DATA_STATUS = "data_status";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}