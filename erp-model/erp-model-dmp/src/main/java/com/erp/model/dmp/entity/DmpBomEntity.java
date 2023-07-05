package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * sku bom关系表
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_bom")
public class DmpBomEntity extends BaseEntity<DmpBomEntity> {


    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;

    /**
    * sku名称
    */
    @TableField("name")
    private String name;

    /**
    * sku用量
    */
    @TableField("qty")
    private Integer qty;

    /**
    * 父sku
    */
    @TableField("parent_sku")
    private String parentSku;

    /**
    * 关系类型 组合：combine 加工：machining
    */
    @TableField("relation_type")
    private String relationType;

    /**
    * 平台标识
    */
    @TableField("platform_sign")
    private String platformSign;

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
    * 有效状态
    */
    @TableField("status")
    private Boolean status;

    @TableField("parent_sku_name")
    private String parentSkuName;


    public static final String SKU_NO = "sku_no";

    public static final String NAME = "name";

    public static final String QTY = "qty";

    public static final String PARENT_SKU = "parent_sku";

    public static final String RELATION_TYPE = "relation_type";

    public static final String PLATFORM_SIGN = "platform_sign";

    public static final String REMARK = "remark";

    public static final String STATUS = "status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}