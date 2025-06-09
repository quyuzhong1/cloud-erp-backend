package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 加工单和销售订单关联表
 * </p>
 *
 * @author will
 * @since 2023-12-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("machine_ref_so")
public class MachineRefSoEntity extends BaseEntity<MachineRefSoEntity> {

    /**
    * 销售订单明细id
    */
    @TableField("so_detail_id")
    private String soDetailId;
    /**
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;

    /**
     * 销售订单编码
     */
    @TableField("so_code")
    private String soCode;
    /**
    * 加工单明细id
    */
    @TableField("machine_detail_id")
    private String machineDetailId;
    /**
    * 加工单id
    */
    @TableField("machine_id")
    private String machineId;



    public static final String SO_DETAIL_ID = "so_detail_id";

    public static final String SO_ID = "so_id";

    public static final String MACHINE_DETAIL_ID = "machine_detail_id";

    public static final String MACHINE_ID = "machine_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}