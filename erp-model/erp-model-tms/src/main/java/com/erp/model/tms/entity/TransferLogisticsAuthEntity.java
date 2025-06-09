package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 物流授权表
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_logistics_auth")
public class TransferLogisticsAuthEntity extends BaseEntity<TransferLogisticsAuthEntity> {

    /**
    * 物流平台
    */
    @TableField("logistics_platform")
    private String logisticsPlatform;
    /**
    * 物流商id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * name
    */
    @TableField("name")
    private String name;


    public static final String LOGISTICS_PLATFORM = "logistics_platform";

    public static final String MAIN_ID = "main_id";

    public static final String FIELD_NAME = "name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}