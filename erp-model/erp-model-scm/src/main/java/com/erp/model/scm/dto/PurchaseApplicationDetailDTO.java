package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 10:34
 */
@Data
@NoArgsConstructor
public class PurchaseApplicationDetailDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 采购申请id
     */
    private String purchaseApplicationId;

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
    @Min(value = 0,message = "计划备货数量最小值为0")
    @Max(value = 99999999,message = "计划备货数量最大值为99999999")
    private Integer unitQty;

    /**
     * 是否加急（false否，true是）
     */
    private Boolean isUrgent;

    /**
     * 计划交期
     */
    private LocalDate planDeliveryDate;

    /**
     * 申请数量
     */
    @NotEmpty(message = "申请数量不能为空")
    @Min(value = 1,message = "申请数量最小值为1")
    @Max(value = 99999999,message = "申请数量最大值为99999999")
    private Integer applyQty;

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
     * 采购组织id
     */
    @NotBlank(message = "采购组织不能为空")
    private String purchaseOrgId;

    /**
     * 采购组织名称
     */
    @NotBlank(message = "采购组织不能为空")
    private String purchaseOrgName;

    /**
     * 收料组织id
     */
    @NotBlank(message = "收料组织不能为空")
    private String receiveOrgId;

    /**
     * 收料组织名称
     */
    @NotBlank(message = "收料组织不能为空")
    private String receiveOrgName;

    /**
     * 备注
     */
    private String remark;


}
