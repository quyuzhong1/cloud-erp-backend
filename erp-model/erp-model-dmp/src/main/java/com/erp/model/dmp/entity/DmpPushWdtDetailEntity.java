package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 推送旺店通中间表明细Entity
 * @date 2024-07-25
 * @author tanmujin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_push_wdt_detail")
public class DmpPushWdtDetailEntity extends BaseEntity<DmpPushWdtDetailEntity> {

    /**
     * 主表ID
     */
    @TableField("main_id")
    private String mainId;

    /**
     * sku no
     */
    @TableField("spec_no")
    private String specNo;

    /**
     * 数量
     */
    @TableField("num")
    private String num;

    /**
     * 仓位编码
     */
    @TableField("position_no")
    private String positionNo;
}
