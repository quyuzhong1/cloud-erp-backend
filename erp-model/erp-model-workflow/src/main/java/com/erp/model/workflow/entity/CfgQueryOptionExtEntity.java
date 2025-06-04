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
 * cfg_query_option拓展表
 * </p>
 *
 * @author jack
 * @since 2025-06-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_query_option_ext")
public class CfgQueryOptionExtEntity extends BaseEntity<CfgQueryOptionExtEntity> {

    /**
    * 主表id
    */
    @TableField("cfg_query_option_id")
    private String cfgQueryOptionId;
    /**
    * 字段类型：class=类，enum=枚举  枚举：CfgQueryOptionExtTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 路径 （类路径，枚举路径）
    */
    @TableField("class_path")
    private String classPath;
    /**
    * json(包含select,table,condition,sys_classify )
    */
    @TableField("data_json")
    private String dataJson;


    public static final String CFG_QUERY_OPTION_ID = "cfg_query_option_id";

    public static final String TYPE = "type";

    public static final String CLASS_PATH = "class_path";

    public static final String DATA_JSON = "data_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
