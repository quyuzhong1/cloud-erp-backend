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
 * 数大臣单据字段
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_cfg_sys_field")
public class DictCfgSysFieldEntity extends BaseEntity<DictCfgSysFieldEntity> {

    /**
    * 字段：对应cfg_process_exp中的field
    */
    @TableField("field")
    private String field;
    /**
    * 请求路径
    */
    @TableField("api_url")
    private String apiUrl;
    /**
    * 请求方法：post,get
    */
    @TableField("request_method")
    private String requestMethod;
    /**
    * 请求参数
    */
    @TableField("param")
    private String param;
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
    * 前端props参数
    */
    @TableField("props")
    private String props;
    /**
    * 接口类型
    */
    @TableField("api_type")
    private String apiType;
    /**
    * 字段类型：checkboxV2=多选，radioV2=单选，input=文本，datetime=日期，number=数值，amount=金额  枚举：DictCfgSysFieldFieldTypeEnum
    */
    @TableField("field_type")
    private String fieldType;
    /**
    * 是否必填
    */
    @TableField("is_required")
    private Boolean isRequired;
    /**
    * 配置单据
    */
    @TableField("bussiness_key")
    private String bussinessKey;
    /**
    * 字段名
    */
    @TableField("field_name")
    private String fieldName;
    /**
    * 字段所属单据类型：table=表头,detail=明细  枚举：DictCfgSysFieldFieldBelongsTypeEnum
    */
    @TableField("field_belongs_type")
    private String fieldBelongsType;


    public static final String FIELD = "field";

    public static final String API_URL = "api_url";

    public static final String REQUEST_METHOD = "request_method";

    public static final String PARAM = "param";

    public static final String SELECT_LABEL = "select_label";

    public static final String SELECT_VALUE = "select_value";

    public static final String SELECT_DISABLED = "select_disabled";

    public static final String SEARCH_KEY_FIELD = "search_key_field";

    public static final String PROPS = "props";

    public static final String API_TYPE = "api_type";

    public static final String FIELD_TYPE = "field_type";

    public static final String IS_REQUIRED = "is_required";

    public static final String BUSSINESS_KEY = "bussiness_key";

    public static final String FIELD_NAME = "field_name";

    public static final String FIELD_BELONGS_TYPE = "field_belongs_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
