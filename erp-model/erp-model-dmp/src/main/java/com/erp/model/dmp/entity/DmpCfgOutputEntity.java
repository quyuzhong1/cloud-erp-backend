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
 * 推送数据配置
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_output")
public class DmpCfgOutputEntity extends BaseEntity<DmpCfgOutputEntity> {

    /**
    * 推送系统id
    */
    @TableField("system_id")
    private String systemId;
    /**
    * 外部系统接口转换内部数据id
    */
    @TableField("input_convert_id")
    private String inputConvertId;
    /**
    * 输入类型：api=接口拉取,mq=MQ订阅,db=DB直连  枚举：DmpCfgOutputTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 输入类型id，api取dmp_cfg_input_api表，mq取dmp_cfg_input_mq表
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
     * 推送速率，一秒推送个数，默认1秒推送3个，小于0不限速
     */
    @TableField("push_rate")
    private Integer pushRate;
    
    /**
     * 输出处理类
     */
     @TableField("output_class")
     private String outputClass;


    public static final String SYSTEM_ID = "system_id";

    public static final String INPUT_CONVERT_ID = "input_convert_id";

    public static final String TYPE = "type";

    public static final String TYPE_ID = "type_id";

    public static final String DISABLED = "disabled";

    public static final String EXTEND_JSON = "extend_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
