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
 * 第三方仓出库
 * </p>
 *
 * @author shukai
 * @since 2024-08-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_outbound")
public class DmpThirdOutboundEntity extends BaseEntity<DmpThirdOutboundEntity> {

    /**
    * 仓库平台类型
    */
    @TableField("warehouse_platform_type")
    private String warehousePlatformType;
    /**
    * 来源平台（编码）：goodcang、iml
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 订单号
    */
    @TableField("order_code")
    private String orderCode;
    /**
    * 入库单状态
    */
    @TableField("reference_no")
    private String referenceNo;
    /**
    * 订单状态
    */
    @TableField("order_status")
    private String orderStatus;
    /**
    * 出库时间
    */
    @TableField("date_shipping")
    private LocalDateTime dateShipping;
    /**
    * 跟踪号
    */
    @TableField("tracking_no")
    private String trackingNo;
    /**
    * 异常原因
    */
    @TableField("abnormal_problem_reason")
    private String abnormalProblemReason;
    
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


    public static final String WAREHOUSE_PLATFORM_TYPE = "warehouse_platform_type";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String ORDER_CODE = "order_code";

    public static final String REFERENCE_NO = "reference_no";

    public static final String ORDER_STATUS = "order_status";

    public static final String DATE_SHIPPING = "date_shipping";

    public static final String TRACKING_NO = "tracking_no";

    public static final String ABNORMAL_PROBLEM_REASON = "abnormal_problem_reason";

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