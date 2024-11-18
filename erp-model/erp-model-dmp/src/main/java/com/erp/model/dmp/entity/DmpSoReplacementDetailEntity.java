package com.erp.model.dmp.entity;

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
 * 中台换货订单明细表
 * </p>
 *
 * @author Jim
 * @since 2024-11-05
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_replacement_detail")
public class DmpSoReplacementDetailEntity extends BaseEntity<DmpSoReplacementDetailEntity> {

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
    * 换货类型
    */
    @TableField("type")
    private String type;
    /**
    * 换货原因
    */
    @TableField("reason")
    private String reason;
    /**
    * 平台SKU
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * 平台产品ID
    */
    @TableField("platform_spu_no")
    private String platformSpuNo;
    /**
    * 换货数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 换货日期
    */
    @TableField("bill_date")
    private LocalDateTime billDate;
    /**
    * 平台换货单号
    */
    @TableField("replacement_code")
    private String replacementCode;
    /**
    * 平台原始单号
    */
    @TableField("original_code")
    private String originalCode;


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String TYPE = "type";

    public static final String REASON = "reason";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String PLATFORM_SPU_NO = "platform_spu_no";

    public static final String QTY = "qty";

    public static final String BILL_DATE = "bill_date";

    public static final String REPLACEMENT_CODE = "replacement_code";

    public static final String ORIGINAL_CODE = "original_code";

}