package com.erp.model.tms.entity;

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
 * 中转报关服务商渠道表
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_logistics_channel")
public class TransferLogisticsChannelEntity extends BaseEntity<TransferLogisticsChannelEntity> {

    /**
    * 渠道名称
    */
    @TableField("name")
    private String name;
    /**
    * 渠道代码
    */
    @TableField("code")
    private String code;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 中转报关服务商表id，表名称：transfer_logistics_supplier
    */
    @TableField("main_id")
    private String mainId;


    public static final String NAME = "name";

    public static final String CODE = "code";

    public static final String DISABLED = "disabled";

    public static final String MAIN_ID = "main_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}