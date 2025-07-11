package com.erp.model.dmp.entity;

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
 * 中台销售订单收货人表
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_receiver")
public class DmpSoReceiverEntity extends BaseEntity<DmpSoReceiverEntity> {

    /**
     * 订单主表id
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 国家二字码
     */
    @TableField("country")
    private String country = "";
    /**
     * 买家账号
     */
    @TableField("buyer_id")
    private String buyerId = "";
    /**
     * 买家姓名
     */
    @TableField("buyer_name")
    private String buyerName = "";
    /**
     * 收货人名称
     */
    @TableField("receiver_name")
    private String receiverName = "";
    /**
     * 收货人电话
     */
    @TableField("receiver_tel_number")
    private String receiverTelNumber = "";
    /**
     * 邮编
     */
    @TableField("post_code")
    private String postCode = "";
    /**
     * 省份
     */
    @TableField("province")
    private String province = "";
    /**
     * 城市
     */
    @TableField("city")
    private String city = "";
    /**
     * 所属区域
     */
    @TableField("district")
    private String district = "";
    /**
     * 街道详细地址
     */
    @TableField("full_address")
    private String fullAddress = "";
    /**
     * 买家地址1
     */
    @TableField("main_street")
    private String mainStreet = "";
    /**
     * 买家地址2
     */
    @TableField("second_street")
    private String secondStreet = "";
    /**
     * 买家电话1
     */
    @TableField("main_phone")
    private String mainPhone = "";
    /**
     * 买家电话2
     */
    @TableField("second_phone")
    private String secondPhone = "";
    /**
     * 地址加密值
     */
    @TableField("oa_id")
    private String oaId = "";
    /**
     * 收货人税号
     */
    @TableField("receiver_tax_no")
    private String receiverTaxNo = "";
    /**
     * 收货人邮箱
     */
    @TableField("email")
    private String email;
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
     * 是否更新订单异常
     */
    @TableField("is_update_error")
    private Boolean isUpdateError;
    /**
     * IE号
     */
    @TableField("ie_no")
    private String ieNo;

    public static final String MAIN_ID = "main_id";

    public static final String COUNTRY = "country";

    public static final String BUYER_ID = "buyer_id";

    public static final String BUYER_NAME = "buyer_name";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String RECEIVER_TEL_NUMBER = "receiver_tel_number";

    public static final String POST_CODE = "post_code";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String DISTRICT = "district";

    public static final String FULL_ADDRESS = "full_address";

    public static final String MAIN_STREET = "main_street";

    public static final String SECOND_STREET = "second_street";

    public static final String MAIN_PHONE = "main_phone";

    public static final String SECOND_PHONE = "second_phone";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String RECEIVER_TAX_NO = "receiver_tax_no";

    public static final String EMAIL = "email";

    @Override
    public Serializable pkVal() {
        return null;
    }

}