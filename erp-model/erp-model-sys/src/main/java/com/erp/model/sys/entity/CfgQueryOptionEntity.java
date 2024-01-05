package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 查询api配置

 * </p>
 *
 * @author lrp
 * @since 2024-01-04
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


    public static final String API_NAME = "api_name";

    public static final String API_URL = "api_url";

    public static final String REQUEST_METHOD = "request_method";

    public static final String PARAM = "param";

    public static final String SELECT_LABEL = "select_label";

    public static final String SELECT_VALUE = "select_value";

    public static final String SELECT_DISABLED = "select_disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}