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
 * 拉取任务文件存储
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_input_task_file")
public class DmpInputTaskFileEntity extends BaseEntity<DmpInputTaskFileEntity> {

    /**
    * 拉取任务id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 文件url
    */
    @TableField("file_url")
    private String fileUrl;
    /**
    * 解析状态：wait=待解析，finish=已解析  枚举：DmpInputTaskFileParseStatusEnum
    */
    @TableField("parse_status")
    private String parseStatus;
    /**
    * 已解析行数
    */
    @TableField("curr_parse_count")
    private Integer currParseCount;
    /**
    * 文件大小
    */
    @TableField("file_size")
    private Integer fileSize;


    public static final String MAIN_ID = "main_id";

    public static final String FILE_URL = "file_url";

    public static final String PARSE_STATUS = "parse_status";

    public static final String CURR_PARSE_COUNT = "curr_parse_count";

    public static final String FILE_SIZE = "file_size";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
