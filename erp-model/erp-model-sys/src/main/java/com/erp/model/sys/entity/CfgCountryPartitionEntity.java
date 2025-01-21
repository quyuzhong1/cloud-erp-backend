package com.erp.model.sys.entity;

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
 * 分区国家关联表
 * </p>
 *
 * @author lrp
 * @since 2025-01-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_country_partition")
public class CfgCountryPartitionEntity extends BaseEntity<CfgCountryPartitionEntity> {

    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 分区id
    */
    @TableField("partition_id")
    private String partitionId;
    /**
    * 分区编码
    */
    @TableField("partition_code")
    private String partitionCode;
    /**
    * 分区名称
    */
    @TableField("partition_name")
    private String partitionName;
    /**
    * 国家二字码
    */
    @TableField("country")
    private String country;
    /**
    * 国家名称
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 排序字段
    */
    @TableField("index")
    private Integer index;


    public static final String DISABLED = "disabled";

    public static final String PARTITION_ID = "partition_id";

    public static final String PARTITION_CODE = "partition_code";

    public static final String PARTITION_NAME = "partition_name";

    public static final String COUNTRY = "country";

    public static final String COUNTRY_NAME = "country_name";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}