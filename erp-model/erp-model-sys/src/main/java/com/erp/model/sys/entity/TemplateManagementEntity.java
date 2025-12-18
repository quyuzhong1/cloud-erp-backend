package com.erp.model.sys.entity;

import java.math.BigDecimal;
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
 * 合同模板主表
 * </p>
 *
 * @author jack
 * @since 2025-07-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("template_management")
public class TemplateManagementEntity extends BaseEntity<TemplateManagementEntity> {

    /**
    * 禁用状态(false:启用,true:禁用)
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 模板编号
    */
    @TableField("code")
    private String code;
    /**
    * 模板名称
    */
    @TableField("name")
    private String name;
    /**
    * 模板类型： contract=合同模板, shippingLabel=面单模板  枚举：TemplateManagementTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 业务类型：purchaseContract=采购框架合同,soContract=销售订单合同  枚举：TemplateManagementBizTypeEnum
    */
    @TableField("biz_type")
    private String bizType;
    /**
    * 状态：finished=已发布,not=未发布,failed=发布失败  枚举：TemplateManagementStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 模板长度(mm)
    */
    @TableField("length")
    private BigDecimal length;
    /**
    * 模板宽度(mm)
    */
    @TableField("width")
    private BigDecimal width;
    /**
    * 前端渲染配置JSON
    */
    @TableField("content")
    private String content;
    /**
    * 模板版本号
    */
    @TableField("template_version")
    private Integer templateVersion;
    /**
    * 前端渲染配置JSON
    */
    @TableField("is_default")
    private Boolean isDefault;
    /**
    * 序号
    */
    @TableField("index")
    private Integer index;


    public static final String DISABLED = "disabled";

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String BIZ_TYPE = "biz_type";

    public static final String STATUS = "status";

    public static final String LENGTH = "length";

    public static final String WIDTH = "width";

    public static final String CONTENT = "content";

    public static final String TEMPLATE_VERSION = "template_version";

    public static final String IS_DEFAULT = "is_default";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
