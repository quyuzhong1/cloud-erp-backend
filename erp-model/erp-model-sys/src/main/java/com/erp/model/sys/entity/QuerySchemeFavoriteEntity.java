package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import com.erp.model.sys.dto.QuerySchemeFavoriteDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.LinkedList;


/**
 * <p>
 * 
 * </p>
 *
 * @author Cloud
 * @since 2023-07-19
*/
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "query_scheme_favorite" , autoResultMap = true)
public class QuerySchemeFavoriteEntity extends BaseEntity<QuerySchemeFavoriteEntity> {


    /**
    * 名称
    */
    @TableField("name")
    private String name;

    /**
    * 用户id
    */
    @TableField("user_id")
    private String userId;

    /**
    * 模块跳转路径
    */
    @TableField("module_path")
    private String modulePath;

    /**
    * 参数json
    */
    @TableField(value = "param_json", typeHandler= JacksonTypeHandler.class)
    private LinkedList<LinkedHashMap<String, Object>> paramJson;

    /**
    * 备注
    */
    @TableField("remark")
    private String remark ;


    public static final String FIELD_NAME = "name";

    public static final String USER_ID = "user_id";

    public static final String MODULE_PATH = "module_path";

    public static final String PARAM_JSON = "param_json";

    public static final String FIELD_REMARK  = "remark ";

    public QuerySchemeFavoriteEntity(QuerySchemeFavoriteDTO.AddDTO addDTO, String userId) {
        this.name = addDTO.getName();
        this.modulePath = addDTO.getModulePath();
        this.paramJson = addDTO.getParamJson();
        this.remark  = addDTO.getRemark ();
        this.userId = userId;
    }

    public QuerySchemeFavoriteEntity(QuerySchemeFavoriteDTO.UpdateDTO updateDTO, String userId) {
        super(updateDTO.getId());
        this.name = updateDTO.getName();
        this.modulePath = updateDTO.getModulePath();
        this.paramJson = updateDTO.getParamJson();
        this.remark  = updateDTO.getRemark ();
    }

}