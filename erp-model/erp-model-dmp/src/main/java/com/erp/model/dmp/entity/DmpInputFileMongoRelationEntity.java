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
 * file与mongo关联表
 * </p>
 *
 * @author shukai
 * @since 2024-06-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_input_file_mongo_relation")
public class DmpInputFileMongoRelationEntity extends BaseEntity<DmpInputFileMongoRelationEntity> {

    /**
    * dmp_input_task_file表主键
    */
    @TableField("file_id")
    private String fileId;
    /**
    * mongo主键
    */
    @TableField("mongo_id")
    private String mongoId;
    
    /**
     * 转换id
     */
    @TableField("convert_id")
    private String convertId;


    public static final String FILE_ID = "file_id";

    public static final String MONGO_ID = "mongo_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}