package com.erp.model.dmp.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;
import org.apache.commons.lang3.StringUtils;


/**
 * <p>
 * 输入信息
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_input")
public class DmpCfgInputEntity extends BaseEntity<DmpCfgInputEntity> {

    /**
    * 系统id
    */
    @TableField("system_id")
    private String systemId;
    /**
    * 数据代码
    */
    @TableField("code")
    private String code;
    /**
    * 数据名称
    */
    @TableField("name")
    private String name;
    /**
    * 输入类型：api=接口拉取,mq=MQ订阅,db=DB直连  枚举：DmpCfgInputTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 输入类型id，api取dmp_cfg_api表，mq取dmp_cfg_mq表
    */
    @TableField("type_id")
    private String typeId;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 扩展json
    */
    @TableField("extend_json")
    private String extendJson;
    /**
    * 是否主任务：true(主任务) false(非主任务)
    */
    @TableField("is_main_task")
    private String isMainTask;
    /**
     * 业务类型：BusinessTypeEnum业务类型
     * 子任务为空
     * 对应dmp_cfg_input的billType
     */
    @TableField("bill_type")
    private String billType;

    /**
     * 执行系统:默认:dmp
     */
    @TableField("exec_system")
    private String execSystem;
    
    public static final String SYSTEM_ID = "system_id";

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String TYPE_ID = "type_id";

    public static final String DISABLED = "disabled";

    public static final String EXTEND_JSON = "extend_json";

}
