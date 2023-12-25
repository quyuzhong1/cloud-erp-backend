package com.erp.model.sys.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.common.core.utils.FastDFSClientUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 文件模板url表
 * </p>
 *
 * @author wangwei
 * @since 2023-12-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("file_template")
public class FileTemplateEntity extends BaseEntity<FileTemplateEntity> {

    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 业务名称
    */
    @TableField("name")
    private String name;
    /**
    * 文件路径url
    */
    @TableField("url")
    private String url;
    /**
    * 文件类型
    */
    @TableField("file_type")
    private String fileType;


    public static final String SOURCE_TYPE = "source_type";

    public static final String NAME = "name";

    public static final String URL = "url";

    public static final String FILE_TYPE = "file_type";

    @Override
    public Serializable pkVal() {
        return null;
    }


    /**
     * 获取fastdf全url
     */
    private String getFastdfsUrl () {
        return StrUtil.format("{}{}",FastDFSClientUtil.publicUrl,this.url);
    }

}