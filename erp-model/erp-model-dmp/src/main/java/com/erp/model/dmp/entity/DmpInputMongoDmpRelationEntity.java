package com.erp.model.dmp.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


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
    
    /**
     * 转换id
     */
     @TableField("convert_id")
     private String convertId;


    public static final String MONGO_ID = "mongo_id";

    public static final String DMP_ID = "dmp_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}