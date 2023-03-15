package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 供应商信息
 * @author yl
 * @Classname SupplierDTO
 * @Description TODO
 * @Date 2023-03-15 16:35
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierDTO  implements Serializable {

    private SupplierBaseDTO   supplierBase;
}
