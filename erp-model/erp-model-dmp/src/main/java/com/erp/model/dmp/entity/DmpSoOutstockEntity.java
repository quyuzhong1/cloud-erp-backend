package com.erp.model.dmp.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 中台销售订单出库详情
 * </p>
 *
 * @author shukai
 * @since 2024-06-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_outstock")
public class DmpSoOutstockEntity extends BaseEntity<DmpSoOutstockEntity> {

    /**
    * 平台创建时间
    */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
    * 平台修改时间
    */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
    /**
    * 来源平台：gyy，kingdee，mabang
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 单据编号（唯一）
    */
    @TableField("third_code")
    private String thirdCode;
    /**
    * 平台原始单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 状态 1.已发货 2.已作废
    */
    @TableField("status")
    private String status;
    /**
    * 平台原始状态
    */
    @TableField("platform_status")
    private String platformStatus;
    /**
    * 发货时间
    */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    /**
    * 物流单号
    */
    @TableField("logistics_code")
    private String logisticsCode;
    /**
    * 店铺编码
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 国家二字码
    */
    @TableField("country")
    private String country;
    /**
    * 买家城市
    */
    @TableField("city")
    private String city;
    /**
    * 买家省份
    */
    @TableField("province")
    private String province;
    /**
    * 买家地址1
    */
    @TableField("man_street")
    private String manStreet;
    /**
    * 买家地址2
    */
    @TableField("second_street")
    private String secondStreet;
    /**
    * 所属区域
    */
    @TableField("district")
    private String district;
    /**
    * 币种
    */
    @TableField("currency_code")
    private String currencyCode;
    /**
    * 单据总金额
    */
    @TableField("all_amount")
    private BigDecimal allAmount;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 运费
    */
    @TableField("shipping_cost")
    private BigDecimal shippingCost;
    /**
    * 补贴金额
    */
    @TableField("subsidy_amount")
    private BigDecimal subsidyAmount;
    
    /**
     * 第三方单号
     */
     @TableField("third_bill_no")
     private String thirdBillNo;
     
     /**
      * 仓管员
      */
     @TableField("stocker_name")
     private String stockerName;
     
     /**
      * 单据日期
      */
     @TableField("bill_date")
     private LocalDateTime billDate;
     
     /**
      * 数据来源
      */
     @TableField("data_sources")
     private String dataSources;
     
     /**
      * 销售组织
      */
     @TableField("sale_org_id")
     private String saleOrgId;
    
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


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_CODE = "third_code";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String STATUS = "status";

    public static final String PLATFORM_STATUS = "platform_status";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String LOGISTICS_CODE = "logistics_code";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String COUNTRY = "country";

    public static final String CITY = "city";

    public static final String PROVINCE = "province";

    public static final String MAN_STREET = "man_street";

    public static final String SECOND_STREET = "second_street";

    public static final String DISTRICT = "district";

    public static final String CURRENCY_CODE = "currency_code";

    public static final String ALL_AMOUNT = "all_amount";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String SHIPPING_COST = "shipping_cost";

    public static final String SUBSIDY_AMOUNT = "subsidy_amount";

    @Override
    public Serializable pkVal() {
        return null;
    }

}