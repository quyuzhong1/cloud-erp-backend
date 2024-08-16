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
 * 第三方区域
 * </p>
 *
 * @author shukai
 * @since 2024-08-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_region")
public class DmpThirdRegionEntity extends BaseEntity<DmpThirdRegionEntity> {

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
    * 父级区域id
    */
    @TableField("parent_region_id")
    private String parentRegionId;
    /**
    * 区域id
    */
    @TableField("region_id")
    private String regionId;
    /**
    * 区域等级
    */
    @TableField("region_level")
    private String regionLevel;
    /**
    * 区域名称
    */
    @TableField("region_name")
    private String regionName;
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

    public static final String PARENT_REGION_ID = "parent_region_id";

    public static final String REGION_ID = "region_id";

    public static final String REGION_LEVEL = "region_level";

    public static final String REGION_NAME = "region_name";

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