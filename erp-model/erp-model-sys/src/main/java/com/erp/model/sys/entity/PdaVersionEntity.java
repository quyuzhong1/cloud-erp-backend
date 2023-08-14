package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pda_version")
public class PdaVersionEntity extends BaseEntity<PdaVersionEntity> {


    /**
    * pda版本
    */
    @TableField("pda_version")
    private String pdaVersion;

    /**
    * 升级内容描述
    */
    @TableField("remark")
    private String remark;

    /**
    * 是否强制更新
    */
    @TableField("force")
    private Boolean force;

    /**
    * 升级包地址
    */
    @TableField("url")
    private String url;


    public static final String PDA_VERSION = "pda_version";

    public static final String REMARK = "remark";

    public static final String FORCE = "force";

    public static final String URL = "url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}