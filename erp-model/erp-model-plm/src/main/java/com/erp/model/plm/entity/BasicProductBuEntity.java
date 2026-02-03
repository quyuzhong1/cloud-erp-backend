package com.erp.model.plm.entity;

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
 * 产品BU信息
 * </p>
 *
 * @author lrp
 * @since 2026-01-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("basic_product_bu")
public class BasicProductBuEntity extends BaseEntity<BasicProductBuEntity> {

    /**
    * 研发团队名称
    */
    @TableField("name")
    private String name;
    /**
    * 是否作废
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 排序
    */
    @TableField("index")
    private Integer index;


    public static final String NAME = "name";

    public static final String DISABLED = "disabled";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}