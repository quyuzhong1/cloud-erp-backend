package com.erp.model.wms.entity;

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
 * 虚拟仓渠道分区关联表
 * </p>
 *
 * @author zdy
 * @since 2025-01-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_warehouse_channel_partition_ref")
public class VirtualWarehouseChannelPartitionRefEntity extends BaseEntity<VirtualWarehouseChannelPartitionRefEntity> {

    /**
    * 排序
    */
    @TableField("index")
    private Integer index;
    /**
    * 虚拟仓库渠道表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 分区id(erp-sys.dict_partition主键)
    */
    @TableField("partition_id")
    private String partitionId;


    public static final String INDEX = "index";

    public static final String MAIN_ID = "main_id";

    public static final String PARTITION_ID = "partition_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}