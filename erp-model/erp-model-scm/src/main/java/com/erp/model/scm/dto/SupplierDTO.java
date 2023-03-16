package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

/**
 * 供应商信息
 *
 * @author yl
 * @Classname SupplierDTO
 * @Description TODO
 * @Date 2023-03-15 16:35
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierDTO implements Serializable {


    /**
     * 供应商基础信息
     */
    @Valid
    private SupplierBaseDTO supplierBase;

    /**
     * 供应商联系信息
     */
    @Valid
    private List<SupplierContactDTO> supplierContactList;

    /**
     * 供应商结算信息
     */
    @Valid
    private List<SupplierAccountDTO> supplierAccountList;

    /**
     * 供应商资质信息
     */
    private List<SupplierCredentialDTO> supplierAptitudesList;
}
