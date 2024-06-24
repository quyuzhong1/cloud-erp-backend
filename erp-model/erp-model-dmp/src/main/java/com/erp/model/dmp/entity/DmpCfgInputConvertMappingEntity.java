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
 * 转换映射
 * </p>
 *
 * @author shukai
 * @since 2024-06-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_input_convert_mapping")
public class DmpCfgInputConvertMappingEntity extends BaseEntity<DmpCfgInputConvertMappingEntity> {

    /**
    * 外部系统接口转换内部数据id
    */
    @TableField("main_id")
    private String mainId;
	
    /**
    * 原始键
    */
    @TableField("original_key")
    private String originalKey;
    /**
    * 转换键
    */
    @TableField("convert_key")
    private String convertKey;

    public static final String ORIGINAL_KEY = "original_key";

    public static final String CONVERT_KEY = "convert_key";

    public static final String MAPPING_STATUS = "mapping_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}