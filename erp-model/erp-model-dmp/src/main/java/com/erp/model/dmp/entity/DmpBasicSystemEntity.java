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
 * 外部系统
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_basic_system")
public class DmpBasicSystemEntity extends BaseEntity<DmpBasicSystemEntity> {

    /**
    * 系统代码：amazon=亚马逊，kingdee=金蝶  枚举：DmpBasicSystemCodeEnum
    */
    @TableField("code")
    private String code;
    /**
    * 系统名称
    */
    @TableField("name")
    private String name;
    /**
    * 系统类型：wms=仓储,tms=物流,finance=财务  枚举：DmpBasicSystemTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
