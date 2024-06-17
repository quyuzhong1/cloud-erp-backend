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
 * data表与dmp关联表
 * </p>
 *
 * @author shukai
 * @since 2024-06-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_input_data_dmp_relation")
public class DmpInputDataDmpRelationEntity extends BaseEntity<DmpInputDataDmpRelationEntity> {

    /**
    * 数据表主键
    */
    @TableField("data_id")
    private String dataId;
    /**
    * dmp主键
    */
    @TableField("dmp_id")
    private String dmpId;
    
    /**
     * 数据类型 DmpInputTaskStatusEnum
     */
     @TableField("data_type")
     private String dataType;


    public static final String DATA_ID = "data_id";

    public static final String DMP_ID = "dmp_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}