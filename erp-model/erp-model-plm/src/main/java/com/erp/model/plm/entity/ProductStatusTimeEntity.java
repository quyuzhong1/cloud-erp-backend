package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:33
 */
@TableName(value ="product_status_time")
@Data
@NoArgsConstructor
public class ProductStatusTimeEntity {

    /**
     * 产品ID
     */
    @TableField(value = "product_id")
    private String productId;

    /**
     * 产品状态
     */
    @TableField(value = "status")
    private String status;

    /**
     * 状态更新时间
     */
    @TableField(value = "status_time")
    private String statusTime;

}
