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
 * 模具存放位置
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mould_store_location")
public class MouldStoreLocationEntity extends BaseEntity<MouldStoreLocationEntity> {

    /**
    * 模具id
    */
    @TableField("mould_detail_id")
    private String mouldDetailId;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 库位
    */
    @TableField("warehouse_location")
    private String warehouseLocation;
    /**
    * 详细地址
    */
    @TableField("address")
    private String address;


    public static final String MOULD_DETAIL_ID = "mould_detail_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String ADDRESS = "address";

    @Override
    public Serializable pkVal() {
        return null;
    }

}