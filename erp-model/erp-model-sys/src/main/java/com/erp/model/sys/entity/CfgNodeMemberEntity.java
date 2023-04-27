package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 节点接收配置表
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_node_member")
public class CfgNodeMemberEntity extends BaseEntity<CfgNodeMemberEntity> {

    /**
     * 节点key
     */
    @TableField("node_key")
    private String nodeKey;

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * 类型名称
     */
    @TableField("type_name")
    private String typeName;

    /**
     * url地址
     */
    @TableField("url")
    private String url;

    /**
     * 请求方式 默认GET
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数以JSON 格式
     */
    @TableField("request_param")
    private String requestParam;

    /**
     * 前端value 字段
     */
    @TableField("value_field")
    private String valueField;

    /**
     * 前端 label 值
     */
    @TableField("label_field")
    private String labelField;

    /**
     * 排序值
     */
    @TableField("sort")
    private String sort;


    public static final String NODE_KEY = "node_key";

    public static final String TYPE = "type";

    public static final String TYPE_NAME = "type_name";

    public static final String URL = "url";

    public static final String REQUEST_METHOD = "request_method";

    public static final String REQUEST_PARAM = "request_param";

    public static final String VALUE_FIELD = "value_field";

    public static final String LABEL_FIELD = "label_field";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
