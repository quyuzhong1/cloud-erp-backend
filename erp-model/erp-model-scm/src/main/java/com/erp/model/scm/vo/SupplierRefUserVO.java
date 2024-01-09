package com.erp.model.scm.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName SupplierRefUserVO
 * @description: TODO
 * @date 2024年01月08日
 * @version: 1.0
 */
@Data
public class SupplierRefUserVO implements Serializable {
    /**
     * 用户id
     */
    private String uid;
    /**
     * 供应商id
     */
    private String supplierId;
    /**
     * 采购员名称
     */
    private String purchaseUserName;
    /**
     * 供应商名称
     */
    private String supplierName;
}
