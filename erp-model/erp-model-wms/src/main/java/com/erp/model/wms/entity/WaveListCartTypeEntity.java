package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 波次列表拣货车类型
 * @date 2024-07-10
 * @author tanmujin
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@TableName("wave_list_cart_type")
public class WaveListCartTypeEntity extends BaseEntity<WaveListCartTypeEntity> implements Serializable {

    @TableField("wave_id")
    private String waveId;

    @TableField("picking_cart_type_id")
    private String pickingCartTypeId;
}
