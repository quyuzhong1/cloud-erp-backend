package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;

import javax.validation.constraints.NotBlank;


/**
 * <p>
 * 输入输出api信息
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_api")
public class DmpCfgApiEntity extends BaseEntity<DmpCfgApiEntity> {

    /**
    * 系统id
    */
    @TableField("system_id")
    private String systemId;
    /**
    * 输入输出类型：input=输入，output=输出  枚举：DmpCfgApiTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * api类型
    */
    @TableField("api_type")
    private String apiType;
    /**
    * api名称
    */
    @TableField("name")
    private String name;
    /**
    * api实现类
    */
    @TableField("api_class")
    private String apiClass;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
     * 业务类型：BusinessTypeEnum业务类型
     * 子任务为空
     * 对应dmp_cfg_input的billType
     */
    @TableField("bill_type")
    private String billType;



    public static final String SYSTEM_ID = "system_id";

    public static final String TYPE = "type";

    public static final String API_TYPE = "api_type";

    public static final String NAME = "name";

    public static final String API_CLASS = "api_class";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
