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
 * 第三方中转仓库
 * </p>
 *
 * @author shukai
 * @since 2024-08-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_transfer_warehouse_info")
public class DmpTransferWarehouseInfoEntity extends BaseEntity<DmpTransferWarehouseInfoEntity> {

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
    * erp授权Id
    */
    @TableField("auth_id")
    private String authId;
    /**
    * 物流渠道编码
    */
    @TableField("logistics_channel_code")
    private String logisticsChannelCode;
    /**
    * 物流渠道名称
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
    * 中转仓编码
    */
    @TableField("transfer_warehouse_code")
    private String transferWarehouseCode;
    /**
    * 中转仓名称
    */
    @TableField("transfer_warehouse_name")
    private String transferWarehouseName;
    /**
    * 目的仓编码
    */
    @TableField("destination_warehouse_code")
    private String destinationWarehouseCode;
    /**
    * 目的仓名称
    */
    @TableField("destination_warehouse_name")
    private String destinationWarehouseName;
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

    public static final String AUTH_ID = "auth_id";

    public static final String LOGISTICS_CHANNEL_CODE = "logistics_channel_code";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String TRANSFER_WAREHOUSE_CODE = "transfer_warehouse_code";

    public static final String TRANSFER_WAREHOUSE_NAME = "transfer_warehouse_name";

    public static final String DESTINATION_WAREHOUSE_CODE = "destination_warehouse_code";

    public static final String DESTINATION_WAREHOUSE_NAME = "destination_warehouse_name";

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