package com.erp.server.wms.service;

import com.erp.model.wms.dto.renovation.SecondarySortingDTO;

import java.util.List;

public interface SecondarySortingService {
    /**
     * 扫描sku
     * @param code 波次编号
     * @see SecondarySortingDTO.ScanSkuView
     */
    SecondarySortingDTO.ScanCodeView scanCode(String code);
    /**
     * 扫描sku
     * @param code 波次编号
     * @see SecondarySortingDTO.ScanSkuView
     */
    SecondarySortingDTO.ScanSkuView scanSku(String code, String skuCode);
    /**
     * 篮子明细
     * @param code 波次编号
     * @param basketNo 篮号
     * @see SecondarySortingDTO.BasketDetail
     */
    List<SecondarySortingDTO.BasketDetail> basketDetail(String code, String basketNo);
    /**
     * 打印配货单
     * @param code 波次编号
     * @see SecondarySortingDTO.BasketDetail
     */
    void printDistribution(String code);
}
