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
 * 销售退货订单主表
 * </p>
 *
 * @author shukai
 * @since 2024-06-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_return_info")
public class DmpSoReturnInfoEntity extends BaseEntity<DmpSoReturnInfoEntity> {

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
    * 退货时间
    */
    @TableField("return_time")
    private LocalDateTime returnTime;
    /**
    * 来源平台：gyy，kingdee，mabang
    */
    @TableField("source_system")
    private String sourceSystem = "";
    /**
    * 第三方单据编号（唯一）
    */
    @TableField("third_code")
    private String thirdCode;
    /**
    * 平台原始单号
    */
    @TableField("platform_code")
    private String platformCode = "";
    /**
    * 店铺编号
    */
    @TableField("shop_id")
    private String shopId = "";
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName = "";
    /**
    * 单据状态：1待处理 2已退款 3已重发 4已完成 5已作废
    */
    @TableField("status")
    private String status = "";
    /**
    * 平台原始状态
    */
    @TableField("platform_status")
    private String platformStatus = "";
    /**
    * 国家二字码
    */
    @TableField("country")
    private String country = "";
    /**
    * 买家账号
    */
    @TableField("buyer_user_id")
    private String buyerUserId = "";
    /**
    * 买家姓名
    */
    @TableField("buyer_name")
    private String buyerName = "";
    /**
    * 备注
    */
    @TableField("remark")
    private String remark = "";
    /**
    * 币种
    */
    @TableField("currency_code")
    private String currencyCode = "";
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 金额
    */
    @TableField("all_amount")
    private BigDecimal allAmount;
    
    /**
     * 销售组织id
     */
     @TableField("sale_org_id")
     private String saleOrgId = "";
     
     /**
      * ulz数据来源
      */
     @TableField("ulz_data_sources")
     private String ulzDataSources = "";
     
     /**
      * 销售部门名称
      */
     @TableField("saledept_name")
     private String saledeptName = "";
     
     /**
      * 销售人名称
      */
     @TableField("sales_man_name")
     private String salesManName = "";
     
     /**
      * 第三方单据编号
      */
     @TableField("third_bill_no")
     private String thirdBillNo = "";
     
     /**
      * 销售组织名称
      */
     @TableField("sale_org_name")
     private String saleOrgName = "";
     
     /**
      * 销售组织编码
      */
     @TableField("saledept_number")
     private String saledeptNumber = "";
     
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
    * 单据时间
    */
    @TableField("bill_date")
    private LocalDateTime billDate;
    /**
    * 卖家运费折扣
    */
    @TableField("shipping_fee_seller_discount")
    private BigDecimal shippingFeeSellerDiscount;
    /**
    * 平台运费折扣
    */
    @TableField("shipping_fee_platform_discount")
    private BigDecimal shippingFeePlatformDiscount;
    /**
    * 退款税费
    */
    @TableField("refund_tax")
    private BigDecimal refundTax;


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String RETURN_TIME = "return_time";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_CODE = "third_code";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String STATUS = "status";

    public static final String PLATFORM_STATUS = "platform_status";

    public static final String COUNTRY = "country";

    public static final String BUYER_USER_ID = "buyer_user_id";

    public static final String BUYER_NAME = "buyer_name";

    public static final String REMARK = "remark";

    public static final String CURRENCY_CODE = "currency_code";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String ALL_AMOUNT = "all_amount";

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