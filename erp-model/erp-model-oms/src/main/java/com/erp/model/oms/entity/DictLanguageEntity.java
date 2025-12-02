package com.erp.model.oms.entity;

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
 * ISO 639-1 语言标准
 * </p>
 *
 * @author jack
 * @since 2025-12-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_language")
public class DictLanguageEntity extends BaseEntity<DictLanguageEntity> {

    /**
    * 编号
    */
    @TableField("code")
    private String code;
    /**
    * 英文名称
    */
    @TableField("name_en")
    private String nameEn;
    /**
    * 中文名称
    */
    @TableField("name_zh")
    private String nameZh;


    public static final String CODE = "code";

    public static final String NAME_EN = "name_en";

    public static final String NAME_ZH = "name_zh";

    @Override
    public Serializable pkVal() {
        return null;
    }

}