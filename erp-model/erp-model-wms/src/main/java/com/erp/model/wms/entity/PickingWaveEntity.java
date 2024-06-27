package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 波次拣货数据库实体
 * @date 2024-06-24
 * @author tanmujin
 */
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("picking_wave")
public class PickingWaveEntity extends BaseEntity<PickingWaveEntity> implements Serializable {
    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("status")
    private String status;

    @TableField("picking_cart_type")
    private String pickingTartType;

    @TableField("picking_cart_code")
    private String pickingCartCode;

    @TableField("pick_type")
    private String pickType;

    @TableField("print_status")
    private String printStatus;

    @TableField("picking_user")
    private String pickingUser;

    @TableField("picking_time")
    private LocalDateTime pickingTime;
}
