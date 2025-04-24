package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;


/**
 * <p>
 * 查询option配置表
 * </p>
 *
 * @author lrp
 * @since 2024-01-05
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "cfg_query_option", autoResultMap = true)
public class CfgQueryOptionEntity extends BaseEntity<CfgQueryOptionEntity> {

    /**
    * 接口名称
    */
    @TableField("api_name")
    private String apiName;
    /**
    * 请求路径
    */
    @TableField("api_url")
    private String apiUrl;
    /**
    * 请求方法 eg:post,get
    */
    @TableField("request_method")
    private String requestMethod;
    /**
    * 请求参数
    */
    @TableField(value = "param", typeHandler = JacksonTypeHandler.class)
    private Map<String,Object> param;
    /**
    * 下拉框显示值
    */
    @TableField("select_label")
    private String selectLabel;
    /**
    * 下拉框绑定值
    */
    @TableField("select_value")
    private String selectValue;
    /**
    * 下拉框禁用绑定字段
    */
    @TableField("select_disabled")
    private String selectDisabled;
    /**
    * 查询绑定属性
    */
    @TableField("search_key_field")
    private String searchKeyField;

    /**
     * 前端props参数 ，json格式,级联选择器必填
     */
    @TableField(value = "props", typeHandler = JacksonTypeHandler.class)
    private Map<String,Object> props;

    /**
     * 接口类型
     */
    @TableField("api_type")
    private String apiType;

    public static final String API_NAME = "api_name";

    public static final String API_URL = "api_url";

    public static final String REQUEST_METHOD = "request_method";

    public static final String FIELD_PARAM = "param";

    public static final String SELECT_LABEL = "select_label";

    public static final String SELECT_VALUE = "select_value";

    public static final String SELECT_DISABLED = "select_disabled";

    public static final String SEARCH_KEY_FIELD = "search_key_field";

    @Override
    public Serializable pkVal() {
        return null;
    }

}