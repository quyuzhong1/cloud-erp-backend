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
 * mongo与dmp关联表
 * </p>
 *
 * @author shukai
 * @since 2024-06-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_input_mongo_dmp_relation")
public class DmpInputMongoDmpRelationEntity extends BaseEntity<DmpInputMongoDmpRelationEntity> {

    /**
    * mongo表主键
    */
    @TableField("mongo_id")
    private String mongoId;
    /**
    * dmp主键
    */
    @TableField("dmp_id")
    private String dmpId;


    public static final String MONGO_ID = "mongo_id";

    public static final String DMP_ID = "dmp_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}