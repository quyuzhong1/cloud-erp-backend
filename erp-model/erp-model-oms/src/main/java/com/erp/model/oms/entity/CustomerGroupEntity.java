package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 客户分组表
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("customer_group")
public class CustomerGroupEntity extends BaseEntity<CustomerGroupEntity> {

    /**
     * 分组名
     */
    @TableField("name")
    private String name;

    /**
     * 排序值
     */
    @TableField("index")
    private Integer index;

    /**
     * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
     */
    @TableField("sync_kingdee_status")
    private String syncKingdeeStatus;

    /**
     * 同步金蝶时间
     */
    @TableField("sync_kingdee_time")
    private LocalDateTime syncKingdeeTime;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 同步操作
     */
    @TableField("sync_operate")
    private String syncOperate;

    public static final String NAME = "name";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
