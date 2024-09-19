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
 * 父子任务关系
 * </p>
 *
 * @author shukai
 * @since 2024-06-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_input_child")
public class DmpCfgInputChildEntity extends BaseEntity<DmpCfgInputChildEntity> {

    /**
    * 父id
    */
    @TableField("parent_id")
    private String parentId;
    /**
    * 子id
    */
    @TableField("child_id")
    private String childId;
    /**
    * 任务状态
    */
    @TableField("input_status")
    private String inputStatus;
    
    /**
     * 转换id
     */
    @TableField("convert_id")
    private String convertId;
    
    /**
     * 事务类型：global全局事务（默认），single单独事务，DmpCfgInputChildTransactionalTypeEnum
     */
    @TableField("transactional_type")
    private String transactionalType;


    public static final String PARENT_ID = "parent_id";

    public static final String CHILD_ID = "child_id";

    public static final String INPUT_STATUS = "input_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}