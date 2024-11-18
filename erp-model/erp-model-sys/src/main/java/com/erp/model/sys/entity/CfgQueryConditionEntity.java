package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 查询条件配置表
 * </p>
 *
 * @author lrp
 * @since 2024-01-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "cfg_query_condition" , autoResultMap = true)
public class CfgQueryConditionEntity extends BaseEntity<CfgQueryConditionEntity> {

    /**
    * 页面code
    */
    @TableField("code")
    private String code;
    /**
    * 字段值,直接在数据库的查询别名
    */
    @TableField("value")
    private String value;
    /**
    * 页面展示值
    */
    @TableField("label")
    private String label;
    /**
    * api配置id,对应表：cfg_query_option
    */
    @TableField("query_option_id")
    private String queryOptionId;
    /**
    * 前端条件控件,eg:input.date,select
    */
    @TableField("controls")
    private String controls;
    /**
    * 数据类型
    */
    @TableField("data_type")
    private String dataType;
    /**
    * 日期类型 eg:month,week
    */
    @TableField("date_type")
    private String dateType;
    /**
    * 前端props参数 ，json格式,级联选择器必填
    */
    @TableField(value = "props", typeHandler = JacksonTypeHandler.class)
    private Map<String,Object> props;

    @TableField("index")
    private Integer index;
    /**
    * 是否扩展字段
    */
    @TableField("is_extend")
    private Boolean isExtend;
    /**
    * 分组名称,用于多个sql跨库查询
    */
    @TableField("group_name")
    private String groupName;
    /**
    * 显示类型
    */
    @TableField("display_type")
    private String displayType;

    /**
     * 自定义option (数组json格式)
     */
    @TableField(value = "option_list", typeHandler = JacksonTypeHandler.class)
    private List<Map<String,Object>> optionList;

    public static final String FIELD_CODE = "code";

    public static final String FIELD_VALUE = "value";

    public static final String FIELD_LABEL = "label";

    public static final String QUERY_OPTION_ID = "query_option_id";

    public static final String FIELD_CONTROLS = "controls";

    public static final String DATA_TYPE = "data_type";

    public static final String DATE_TYPE = "date_type";

    public static final String FIELD_PROPS = "props";

    public static final String FIELD_INDEX = "index";

    public static final String IS_EXTEND = "is_extend";

    @Override
    public Serializable pkVal() {
        return null;
    }

}