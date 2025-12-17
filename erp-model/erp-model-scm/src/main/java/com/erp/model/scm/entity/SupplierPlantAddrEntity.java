package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 供应商工厂地信息
 * </p>
 *
 * @author will
 * @since 2025-07-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("supplier_plant_addr")
public class SupplierPlantAddrEntity extends BaseEntity<SupplierPlantAddrEntity> {

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 国家id
    */
    @TableField("country")
    private String country;
    /**
    * 省份id
    */
    @TableField("region")
    private String region;
    /**
    * 城市id
    */
    @TableField("city")
    private String city;


    public static final String SUPPLIER_ID = "supplier_id";

    public static final String COUNTRY = "country";

    public static final String REGION = "region";

    public static final String CITY = "city";

    @Override
    public Serializable pkVal() {
        return null;
    }

}