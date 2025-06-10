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
 * 中台原始销售订单明细表
 * </p>
 *
 * @author shukai
 * @since 2024-11-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_original_detail")
public class DmpSoOriginalDetailEntity extends BaseEntity<DmpSoOriginalDetailEntity> {

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
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 第三方明细id
    */
    @TableField("third_detail_id")
    private String thirdDetailId;
    /**
    * 状态
    */
    @TableField("status")
    private String status;
    /**
    * 平台货品名称
    */
    @TableField("goods_name")
    private String goodsName;
    /**
    * 平台货品编号
    */
    @TableField("goods_no")
    private String goodsNo;
    /**
    * 数量
    */
    @TableField("num")
    private BigDecimal num;
    /**
    * 单价
    */
    @TableField("price")
    private BigDecimal price;
    /**
    * 分摊优惠
    */
    @TableField("share_discount")
    private BigDecimal shareDiscount;
    /**
    * 退款金额
    */
    @TableField("refund_amount")
    private BigDecimal refundAmount;
    /**
    * 物流单号
    */
    @TableField("logistics_no")
    private String logisticsNo;
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

    public static final String MAIN_ID = "main_id";

    public static final String THIRD_DETAIL_ID = "third_detail_id";

    public static final String STATUS = "status";

    public static final String GOODS_NAME = "goods_name";

    public static final String GOODS_NO = "goods_no";

    public static final String NUM = "num";

    public static final String PRICE = "price";

    public static final String SHARE_DISCOUNT = "share_discount";

    public static final String REFUND_AMOUNT = "refund_amount";

    public static final String LOGISTICS_NO = "logistics_no";

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