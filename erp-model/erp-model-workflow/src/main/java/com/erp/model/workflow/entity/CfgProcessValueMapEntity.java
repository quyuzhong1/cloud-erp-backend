package com.erp.model.workflow.entity;

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
 * 流程设置值映射
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_process_value_map")
public class CfgProcessValueMapEntity extends BaseEntity<CfgProcessValueMapEntity> {

    /**
    * 对应的字段配置ID
    */
    @TableField("field_map_id")
    private String fieldMapId;
    /**
    * 第三方选项（显示文本）
    */
    @TableField("third_value")
    private String thirdValue;
    /**
    * 数大臣选项值
    */
    @TableField("sys_value")
    private String sysValue;

    @TableField("default_value")
    private String defaultValue;

    public static final String FIELD_MAP_ID = "field_map_id";

    public static final String THIRD_VALUE = "third_value";

    public static final String SYS_VALUE = "sys_value";

    public static final String DEFAULT_VALUE = "default_value";

    @Override
    public Serializable pkVal() {
        return null;
    }

}