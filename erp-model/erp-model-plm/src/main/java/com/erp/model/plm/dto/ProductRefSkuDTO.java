package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 产品关联SKU页签明细
 *
 * @author codex
 * @since 2026-04-10
 */
@Data
@NoArgsConstructor
public class ProductRefSkuDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 当前/所属sku id
     */
    private String skuId;

    /**
     * 当前/所属sku编码
     */
    private String skuNo;

    /**
     * 变体属性
     */
    private String variantProperty;

    /**
     * 关联sku id
     */
    private String refSkuId;

    /**
     * 关联sku编码
     */
    private String refSkuNo;

    /**
     * 关联产品名称
     */
    private String refProductName;

    /**
     * 备注
     */
    @Size(max = 200, message = "关联SKU备注最大200字符")
    private String remark;

    /**
     * 排序
     */
    private Integer sort;
}
