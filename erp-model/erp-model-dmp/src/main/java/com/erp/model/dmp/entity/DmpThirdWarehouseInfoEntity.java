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
 * 第三方仓库
 * </p>
 *
 * @author shukai
 * @since 2024-08-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_warehouse_info")
public class DmpThirdWarehouseInfoEntity extends BaseEntity<DmpThirdWarehouseInfoEntity> {

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
    * 仓库编码
    */
    @TableField("warehouse_code")
    private String warehouseCode;
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 国家编码
    */
    @TableField("country_code")
    private String countryCode;
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


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String WAREHOUSE_PLATFORM_TYPE = "warehouse_platform_type";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String AUTH_ID = "auth_id";

    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String COUNTRY_CODE = "country_code";

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