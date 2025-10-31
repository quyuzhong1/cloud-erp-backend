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
 * 平台文件转存FastDFS关系记录表
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_ref_platform_file")
public class DmpRefPlatformFileEntity extends BaseEntity<DmpRefPlatformFileEntity> {

    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 任务转换ID
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 店铺ID
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 任务来源唯一加密代号
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 任务数据加密代号
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 来源平台编码
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 来源文件URL
    */
    @TableField("source_url")
    private String sourceUrl;
    /**
    * FastDFS文件URL
    */
    @TableField("file_url")
    private String fileUrl;
    /**
    * 文件类型（json/png/jpeg等）
    */
    @TableField("file_type")
    private String fileType;
    /**
    * 业务类型
    */
    @TableField("bill_topic")
    private String billTopic;
    /**
    * 业务ID
    */
    @TableField("bill_id")
    private String billId;
    /**
    * 文件唯一标识
    */
    @TableField("file_key")
    private String fileKey;
    /**
    * 文件名称
    */
    @TableField("file_name")
    private String fileName;


    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String SOURCE_URL = "source_url";

    public static final String FILE_URL = "file_url";

    public static final String FILE_TYPE = "file_type";

    public static final String BILL_TOPIC = "bill_topic";

    public static final String BILL_ID = "bill_id";

    public static final String FILE_KEY = "file_key";

    public static final String FILE_NAME = "file_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}