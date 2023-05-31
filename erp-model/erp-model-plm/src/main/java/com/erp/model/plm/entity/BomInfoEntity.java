package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * bom 信息表(BomInfo)实体类
 *
 * @author yl
 * @since 2023-01-09 11:31:06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_info")
public class BomInfoEntity extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 458249379651802162L;

    /**
     * 编号
     */
    private String serialNumber;
    /**
     * 类型combination 组合 single 单品
     */
    private String type;
    /**
     * 状态
     */
    private Integer state;

    /**
     * 备注
     */
    private String remark;

    /**
     * 同步金蝶状态（默认0无需发送,1待发送,2发送成功,3发送失败）
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
     * 父级skuId
     */
    @TableField(exist = false)
    private String parentSkuId;
}

