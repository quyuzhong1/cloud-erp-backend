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
 * 中台销售订单出库库位详情
 * </p>
 *
 * @author shukai
 * @since 2024-07-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_logistic_info")
public class DmpLogisticInfoEntity extends BaseEntity<DmpLogisticInfoEntity> {

    /**
    * 主id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 物流单号
    */
    @TableField("logistics_no")
    private String logisticsNo;
    /**
    * 发货时间
    */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    /**
    * 物流服务商
    */
    @TableField("logistics_service_name")
    private String logisticsServiceName;
    /**
    * 物流类型代码
    */
    @TableField("logistics_type_code")
    private String logisticsTypeCode;
    /**
    * 发货状态
    */
    @TableField("receive_status")
    private String receiveStatus;
    /**
     * 币种编码
     */
     @TableField("currency_code")
     private String currencyCode = "";
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


    public static final String MAIN_ID = "main_id";

    public static final String LOGISTICS_NO = "logistics_no";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String LOGISTICS_SERVICE_NAME = "logistics_service_name";

    public static final String LOGISTICS_TYPE_CODE = "logistics_type_code";

    public static final String RECEIVE_STATUS = "receive_status";

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