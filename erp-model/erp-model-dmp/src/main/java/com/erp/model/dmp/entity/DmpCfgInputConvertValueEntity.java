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
 * 
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_input_convert_value")
public class DmpCfgInputConvertValueEntity extends BaseEntity<DmpCfgInputConvertValueEntity> {

    /**
    * 映射dmp_cfg_input_convert_mapping表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 转换后的值
    */
    @TableField("convert_after_value")
    private String convertAfterValue;
    /**
    * 转换前的值
    */
    @TableField("convert_before_value")
    private String convertBeforeValue;


    public static final String MAIN_ID = "main_id";

    public static final String CONVERT_AFTER_VALUE = "convert_after_value";

    public static final String CONVERT_BEFORE_VALUE = "convert_before_value";

    @Override
    public Serializable pkVal() {
        return null;
    }

}