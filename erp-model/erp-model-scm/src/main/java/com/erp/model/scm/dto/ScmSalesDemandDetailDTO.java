package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 17:36
 */
@Data
@NoArgsConstructor
public class ScmSalesDemandDetailDTO implements Serializable {

    /**
     * skuId
     */
    private String skuId;

    /**
     * sku编码
     */
    private String skuNo;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 单箱数量
     */
    private Integer qty;

    /**
     * 是否加急（false否，true是）
     */
    private Boolean isUrgent;

    /**
     * 计划交期
     */
    private Date planDeliveryDate;

    /**
     * 计划备货数量
     */
    @NotEmpty(message = "计划备货量不能为空")
    @Min(value = 1,message = "计划备货数量必须大于0")
    private Integer planStockQty;

    /**
     * 目的仓库id
     */
    @NotBlank(message = "目的仓库不能为空")
    private String destWarehouseId;

    /**
     * 目的仓库名称
     */
    @NotBlank(message = "目的仓库不能为空")
    private String destWarehouseName;

    /**
     * 备注
     */
    private String remark;
}
