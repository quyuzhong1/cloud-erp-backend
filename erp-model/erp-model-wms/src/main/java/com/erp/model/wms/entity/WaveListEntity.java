package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.WaveStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 波次列表数据库实体
 * @date 2024-06-24
 * @author tanmujin
 */
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("wave_list")
public class WaveListEntity extends BaseEntity<WaveListEntity> implements Serializable {
    /**
     * 波次编码
     */
    @TableField("code")
    private String code;

    /**
     * 波次名称
     */
    @TableField("name")
    private String name;

    /**
     * 波次类型
     */
    @TableField("type")
    private String type;

    /**
     * 波次状态<br/>
     * @see WaveStatusEnum
     */
    @TableField("status")
    private String status;

    /**
     * 拣货车类型(ID)
     */
    @TableField("picking_cart_type")
    private String pickingCartType;

    /**
     * 拣货车编码
     */
    @TableField("picking_cart_code")
    private String pickingCartCode;

    /**
     * 分拣方式
     */
    @TableField("picking_type")
    private String pickingType;

    /**
     * 打印状态
     */
    @TableField("print_status")
    private String printStatus;

    /**
     * 打印时间
     */
    @TableField("print_time")
    private String printTime;

    /**
     * 拣货单打印状态
     */
    @TableField("picking_print_status")
    private String pickingPrintStatus;

    /**
     * 拣货单打印时间
     */
    @TableField("picking_print_time")
    private String pickingPrintTime;

    /**
     * 拣货人
     */
    @TableField("picking_user_id")
    private String pickingUserId;

    /**
     * 拣货人名称
     */
    @TableField("picking_user_name")
    private String pickingUserName;

    /**
     * 拣货时间
     */
    @TableField("picking_time")
    private LocalDateTime pickingTime;

    /**
     * 波次是否缺货
     */
    @TableField("is_out_stock")
    private Boolean isOutStock;
}
