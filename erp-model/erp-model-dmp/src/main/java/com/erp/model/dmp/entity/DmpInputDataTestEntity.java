package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 
 * </p>
 *
 * @author shukai
 * @since 2024-06-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_input_data_test")
public class DmpInputDataTestEntity extends BaseEntity<DmpInputDataTestEntity> {
	
	/**
	 * 输入任务id
	 */
	@TableField("input_task_id")
    private String inputTaskId;
	/**
	 * 转换id
	 */
	@TableField("convert_id")
    private String convertId;
    /**
     * 下一层级
     */
    @TableField("next_level_id")
    private String nextLevelId;
    
    /**
	 * 唯一字段md5值
	 */
	@TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
     * 数据字段md5值
     */
    @TableField("data_encrypt")
    private String dataEncrypt;
    
    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;
    
    @TableField("code")
    private String code;
    @TableField("name")
    private String name;
    @TableField("size")
    private Integer size;

    public static final String INPUT_TASK_ID = "input_task_id";
    
    public static final String CONVERT_ID = "convert_id";

    public static final String FILE_ID = "file_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";
    
    public static final String CODE = "code";

    public static final String NAME = "name";

}