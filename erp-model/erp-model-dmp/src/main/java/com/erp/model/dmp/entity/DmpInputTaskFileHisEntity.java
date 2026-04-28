package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 拉取任务文件存储归档
 * </p>
 *
 * @author shukai
 * @since 2026-01-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_input_task_file_his")
public class DmpInputTaskFileHisEntity extends BaseEntity<DmpInputTaskFileHisEntity> {

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
    * 解析状态：wait=待解析，finish=已解析
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
    /**
    * 文件内容形式
    */
    @TableField("content_type")
    private String contentType;
    /**
    * 外部系统接口转换init内部数据id
    */
    @TableField("init_convert_id")
    private String initConvertId;
    /**
    * 外部系统接口转换fds内部数据id
    */
    @TableField("fds_convert_id")
    private String fdsConvertId;


    public static final String MAIN_ID = "main_id";

    public static final String FILE_URL = "file_url";

    public static final String PARSE_STATUS = "parse_status";

    public static final String CURR_PARSE_COUNT = "curr_parse_count";

    public static final String FILE_SIZE = "file_size";

    public static final String CONTENT_TYPE = "content_type";

    public static final String INIT_CONVERT_ID = "init_convert_id";

    public static final String FDS_CONVERT_ID = "fds_convert_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}