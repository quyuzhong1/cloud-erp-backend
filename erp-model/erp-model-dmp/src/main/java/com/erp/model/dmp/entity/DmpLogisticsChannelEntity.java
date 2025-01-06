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
 * 中台三方物流渠道表
 * </p>
 *
 * @author Jim
 * @since 2024-12-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_logistics_channel")
public class DmpLogisticsChannelEntity extends BaseEntity<DmpLogisticsChannelEntity> {

    /**
     * 平台类型：lingxing领星
     */
    @TableField("platform_type")
    private String platformType;
    /**
     * 物流商类型
     */
    @TableField("type")
    private String type;
    /**
     * 物流商id
     */
    @TableField("logistics_supplier_id")
    private String logisticsSupplierId;
    /**
     * 物流商名称
     */
    @TableField("logistics_supplier_name")
    private String logisticsSupplierName;
    /**
     * 是否禁用/停用 true 是 false 不是
     */
    @TableField("disabled")
    private Boolean disabled;
    /**
     * 物流方式id
     */
    @TableField("logistics_type_id")
    private String logisticsTypeId;
    /**
     * 物流方式名称
     */
    @TableField("logistics_type_name")
    private String logisticsTypeName;
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


    public static final String PLATFORM_TYPE = "platform_type";

    public static final String TYPE = "type";

    public static final String LOGISTICS_SUPPLIER_ID = "logistics_supplier_id";

    public static final String LOGISTICS_SUPPLIER_NAME = "logistics_supplier_name";

    public static final String DISABLED = "disabled";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String LOGISTICS_TYPE_ID = "logistics_type_id";

    public static final String LOGISTICS_TYPE_NAME = "logistics_type_name";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

}