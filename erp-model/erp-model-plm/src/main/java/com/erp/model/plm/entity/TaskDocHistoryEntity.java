package com.erp.model.plm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 任务文档历史表
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("task_doc_history")
public class TaskDocHistoryEntity extends BaseEntity<TaskDocHistoryEntity> {


    /**
    * 任务id
    */
    @TableField("task_id")
    private String taskId;

    /**
    * 完成交付物的表id 对应task_finish_docs表
    */
    @TableField("finish_doc_id")
    private String finishDocId;

    /**
    * 文件名
    */
    @TableField("file_name")
    private String fileName;

    /**
    * 文件地址
    */
    @TableField("file_url")
    private String fileUrl;

    /**
    * 交付文档表id  task_delivery_docs表id
    */
    @TableField("require_doc_id")
    private String requireDocId;

    /**
    * 文件类型
    */
    @TableField("file_type")
    private String fileType;

    /**
    * 文件大小
    */
    @TableField("file_size")
    private BigDecimal fileSize;

    /**
    * 文件后缀
    */
    @TableField("file_suffix")
    private String fileSuffix;

    /**
    * 产品id
    */
    @TableField("product_id")
    private String productId;

    /**
    * 上传类型 0 本地  1 链接
    */
    @TableField("upload_type")
    private Integer uploadType;


    public static final String TASK_ID = "task_id";

    public static final String FINISH_DOC_ID = "finish_doc_id";

    public static final String FILE_NAME = "file_name";

    public static final String FILE_URL = "file_url";

    public static final String REQUIRE_DOC_ID = "require_doc_id";

    public static final String FILE_TYPE = "file_type";

    public static final String FILE_SIZE = "file_size";

    public static final String FILE_SUFFIX = "file_suffix";

    public static final String PRODUCT_ID = "product_id";

    public static final String UPLOAD_TYPE = "upload_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}