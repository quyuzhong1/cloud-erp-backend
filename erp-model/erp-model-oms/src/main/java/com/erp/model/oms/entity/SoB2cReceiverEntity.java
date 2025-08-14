package com.erp.model.oms.entity;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.ReflectUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


/**
 * <p>
 * B2C销售订单买家信息表
 * </p>
 *
 * @author Will
 * @since 2023-08-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_receiver")
public class SoB2cReceiverEntity extends BaseEntity<SoB2cReceiverEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
        private String mainId;
    /**
    * 买家登录id
    */
    @TableField("login_id")
        private String loginId;
    /**
    * 买家id
    */
    @TableField("customer_id")
        private String customerId;
    /**
    * 买家全名
    */
    @TableField("name")
        private String name;
    /**
    * 邮箱
    */
    @TableField("email")
        private String email;
    /**
    * 买家电话
    */
    @TableField("tel_number")
        private String telNumber;
    /**
    * 收货地址1
    */
    @TableField("first_address")
        private String firstAddress;
    /**
    * 收货地址2
    */
    @TableField("second_address")
        private String secondAddress;
    /**
    * 城市名称
    */
    @TableField("city_name")
        private String cityName;
    /**
    * 国家名称
    */
    @TableField("country_name")
        private String countryName;
    /**
    * 收货人名称
    */
    @TableField("receiver_name")
        private String receiverName;
    /**
    * 收货人电话
    */
    @TableField("receiver_tel_number")
        private String receiverTelNumber;
    /**
    * 邮编
    */
    @TableField("post_code")
        private String postCode;
    /**
    * 街道详细地址
    */
    @TableField("full_address")
        private String fullAddress;
    /**
     * 国家二字码
     */
    @TableField("country")
    private String country;
    /**
     * 省
     */
    @TableField("province_name")
    private String provinceName;
    /**
     * 区
     */
    @TableField("district_name")
    private String districtName;

    /**
     * 收货人税号
     */
    @TableField("receiver_tax_no")
    private String receiverTaxNo;

    /**
     * 分区id
     */
    @TableField("partition_id")
    private String partitionId;

    /**
     * 开票地址
     */
    @TableField("invoice_address")
    private String invoiceAddress;

    /**
     * IE号
     */
    @TableField("ie_no")
    private String ieNo;
    /**
     * 证件类型（CPF自然人，CNPJ公司）
     */
    @TableField("taxid_type")
    private String taxidType;


    @TableField(exist = false)
    private String partitionCode;

    @TableField(exist = false)
    private String partitionName;

    @TableField(exist = false)
    private String customerCountry;

    @TableField(exist = false)
    private String shopCountry;

    public static final String MAIN_ID = "main_id";

    public static final String LOGIN_ID = "login_id";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String NAME = "name";

    public static final String EMAIL = "email";

    public static final String TEL_NUMBER = "tel_number";

    public static final String FIRST_ADDRESS = "first_address";

    public static final String SECOND_ADDRESS = "second_address";

    public static final String CITY_ID = "city_id";

    public static final String COUNTRY_ID = "country_id";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String RECEIVER_TEL_NUMBER = "receiver_tel_number";

    public static final String POST_CODE = "post_code";

    public static final String FULL_ADDRESS = "full_address";

    /**
     * 有值不更新的字段
     */
    public static List<String> fieldsExistNotUpdate(){
        return Arrays.asList(
                "name",
                "email",
                "telNumber",
                "firstAddress",
                "secondAddress",
                "cityName",
                "countryName",
                "receiverName",
                "receiverTelNumber",
                "postCode",
                "fullAddress",
                "country",
                "provinceName",
                "districtName",
                "receiverTaxNo"
        );
    }


    /**
     * 检查来源
     */
    public void checkAndSetCountry(String sourceCountry, String countryId) {
        if (sourceCountry.matches("[A-Z]{3}") ){
            this.setCountry(countryId);
        }
    }
}