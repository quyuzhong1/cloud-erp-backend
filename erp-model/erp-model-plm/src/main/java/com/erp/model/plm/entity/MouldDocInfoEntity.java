package com.erp.model.plm.entity;

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
 * 模具文档信息
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mould_doc_info")
public class MouldDocInfoEntity extends BaseEntity<MouldDocInfoEntity> {

    /**
    * 模具信息
    */
    @TableField("mould_info_id")
    private String mouldInfoId;
    /**
    * 文档类型id
    */
    @TableField("type_id")
    private String typeId;
    /**
    * 版本号
    */
    @TableField("doc_version")
    private String docVersion;
    /**
    * 文件地址
    */
    @TableField("doc_url")
    private String docUrl;
    /**
    * 文档名字
    */
    @TableField("doc_name")
    private String docName;
    /**
    * 外部链接
    */
    @TableField("ext_link")
    private String extLink;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MOULD_INFO_ID = "mould_info_id";

    public static final String TYPE_ID = "type_id";

    public static final String DOC_VERSION = "doc_version";

    public static final String DOC_URL = "doc_url";

    public static final String DOC_NAME = "doc_name";

    public static final String EXT_LINK = "ext_link";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}