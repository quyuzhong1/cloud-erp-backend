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
 * 中台销售退款单明细表
 * </p>
 *
 * @author shukai
 * @since 2024-07-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_refund_detail")
public class DmpSoRefundDetailEntity extends BaseEntity<DmpSoRefundDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 来源详情id
    */
    @TableField("third_detail_id")
    private String thirdDetailId;
    /**
    * 销售平台原始详情id
    */
    @TableField("platform_detail_id")
    private String platformDetailId;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
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
     * 当前明细总税费(币种跟主单一致)
     */
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    /**
     * 拓展字段
     */
    @TableField("extend_data")
    private String extendData = "{}";
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
     * 来源订单明细id
     */
    @TableField("src_order_detail_id")
    private String srcOrderDetailId = "";


    public static final String MAIN_ID = "main_id";

    public static final String THIRD_DETAIL_ID = "third_detail_id";

    public static final String PLATFORM_DETAIL_ID = "platform_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String AMOUNT = "amount";

    public static final String IS_GIFT = "is_gift";

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