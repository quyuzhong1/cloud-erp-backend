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
 * 推送字段映射表
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-15
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_output_convert_mapping")
public class DmpCfgOutputConvertMappingEntity extends BaseEntity<DmpCfgOutputConvertMappingEntity> {

    /**
    * dmp_cfg_output表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 原始字段
    */
    @TableField("original_key")
    private String originalKey;
    /**
    * 转换后字段
    */
    @TableField("convert_key")
    private String convertKey;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 默认值
    */
    @TableField("default_value")
    private String defaultValue;
    /**
    * 组别类型（0正常级别，1集合父项，2集合子项）
    */
    @TableField("group_type")
    private String groupType;
    /**
    * 取值方式（0 直接复制，1 按对照表赋值）
    */
    @TableField("field_type")
    private Integer fieldType;
    /**
    * 集合父项id
    */
    @TableField("parent_id")
    private String parentId;


    public static final String MAIN_ID = "main_id";

    public static final String ORIGINAL_KEY = "original_key";

    public static final String CONVERT_KEY = "convert_key";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}