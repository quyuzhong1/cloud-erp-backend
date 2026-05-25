package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * <p>
 * 报关单
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_declare_bill")
public class TmsDeclareBillEntity extends BaseEntity<TmsDeclareBillEntity> {
    /**
     * 类型：头程,B2B
     */
    @TableField("type")
    private String type;
    /**
    * 合同协议号
    */
    @TableField("code")
    private String code;
    /**
    * 报关状态
    */
    @TableField("declare_status")
    private String declareStatus;
    /**
    * 发货类型
    */
    @TableField("business_type")
    private String businessType;

    /**
     * 提运单号，头程的话这个值是空的，B2B的话是页面手动输入
     */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 目的国家
    */
    @TableField("country")
    private String country;

    /**
     * 目的国家名称
     */
    @TableField("country_name")
    private String countryName;
    /**
    * 总净重
    */
    @TableField("net_weight")
    private BigDecimal netWeight;
    /**
    * 总毛重
    */
    @TableField("gross_weight")
    private BigDecimal grossWeight;
    /**
    * 报关日期
    */
    @TableField(value = "declare_date")
    private LocalDate declareDate;
    /**
    * 报关确认日期
    */
    @TableField(value = "declare_confirm_date")
    private LocalDate declareConfirmDate;
    /**
    * 报关确认人id
    */
    @TableField(value = "declare_user_id")
    private String declareUserId;
    /**
    * 报关确认人
    */
    @TableField(value = "declare_user_name")
    private String declareUserName;
    /**
    * 报关类型
    */
    @TableField("declare_type")
    private String declareType;
    /**
    * 预录入编号
    */
    @TableField("pre_input_no")
    private String preInputNo;
    /**
    * 申报地海关
    */
    @TableField("dest_customs")
    private String destCustoms;
    /**
    * 发货人id
    */
    @TableField("sender_id")
    private String senderId;
    /**
     * 发货人名称
     */
    @TableField("sender_name")
    private String senderName;
    /**
     * 发货人类型
     */
    @TableField("sender_type")
    private String senderType;

    /**
    * 出境关别
    */
    @TableField("export_customs_name")
    private String exportCustomsName;
    /**
    * 出口日期
    */
    @TableField("export_date")
    private LocalDate exportDate;
    /**
     * 收货人id
     */
    @TableField("receiver_id")
    private String receiverId;
    /**
    * 收货人名称
    */
    @TableField("receiver_name")
    private String receiverName;
    /**
     * 收货人类型
     */
    @TableField("receiver_type")
    private String receiverType;
    /**
    * 监管方式
    */
    @TableField("dict_supervision_method")
    private String dictSupervisionMethod;
    /**
    * 征免性质
    */
    @TableField("dict_nature_levy")
    private String dictNatureLevy;
    /**
    * 许可证号
    */
    @TableField("license_no")
    private String licenseNo;
    /**
    * 贸易国
    */
    @TableField("trading_area")
    private String tradingArea;
    /**
    * 运抵国
    */
    @TableField("to_area")
    private String toArea;
    /**
    * 运抵港
    */
    @TableField("to_port")
    private String toPort;
    /**
    * 出境口岸
    */
    @TableField("export_port")
    private String exportPort;
    /**
    * 包装种类
    */
    @TableField("dict_pack_type")
    private String dictPackType;
    /**
    * 成交方式
    */
    @TableField("dict_transaction_method")
    private String dictTransactionMethod;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 运费
    */
    @TableField("shipping_fee")
    private BigDecimal shippingFee;
    /**
    * 保费
    */
    @TableField("insurance_fee")
    private BigDecimal insuranceFee;
    /**
    * 杂费
    */
    @TableField("other_fee")
    private BigDecimal otherFee;
    /**
    * 总箱数
    */
    @TableField("box_qty")
    private Integer boxQty;


    public static final String FIELD_CODE = "code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String DECLARE_STATUS = "declare_status";

    public static final String LOGISTICS_SUPPLIER_ID = "logistics_supplier_id";

    public static final String LOGISTICS_SUPPLIER_NAME = "logistics_supplier_name";

    public static final String DELIVERY_TYPE = "delivery_type";

    public static final String FIELD_COUNTRY = "country";

    public static final String NET_WEIGHT = "net_weight";

    public static final String GROSS_WEIGHT = "gross_weight";

    public static final String DECLARE_DATE = "declare_date";

    public static final String DECLARE_CONFIRM_DATE = "declare_confirm_date";

    public static final String DECLARE_USER_ID = "declare_user_id";

    public static final String DECLARE_USER_NAME = "declare_user_name";

    public static final String DECLARE_TYPE = "declare_type";

    public static final String PRE_INPUT_NO = "pre_input_no";

    public static final String DEST_CUSTOMS = "dest_customs";

    public static final String SENDER_ID = "sender_id";

    public static final String SENDER_NAME = "sender_name";

    public static final String EXPORT_CUSTOMS_NAME = "export_customs_name";

    public static final String EXPORT_DATE = "export_date";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String DICT_SUPERVISION_METHOD = "dict_supervision_method";

    public static final String DICT_NATURE_LEVY = "dict_nature_levy";

    public static final String LICENSE_NO = "license_no";

    public static final String TRADING_AREA = "trading_area";

    public static final String TO_AREA = "to_area";

    public static final String TO_PORT = "to_port";

    public static final String EXPORT_PORT = "export_port";

    public static final String DICT_PACK_TYPE = "dict_pack_type";

    public static final String DICT_TRANSACTION_METHOD = "dict_transaction_method";

    public static final String FIELD_REMARK = "remark";

    public static final String SHIPPING_FEE = "shipping_fee";

    public static final String INSURANCE_FEE = "insurance_fee";

    public static final String OTHER_FEE = "other_fee";

    public static final String BOX_QTY = "box_qty";

    public static final String IS_INVALID = "is_invalid";

    public static final String MERGED_CODE = "merged_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}