package com.erp.server.wms.service;

import com.erp.model.wms.dto.renovation.SecondarySortingDTO;

import javax.servlet.http.HttpServletResponse;
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
     * @param waveId 波次id
     * @see SecondarySortingDTO.ScanSkuView
     */
    SecondarySortingDTO.ScanSkuView scanSku(String waveId, String skuCode);
    /**
     * 篮子明细
     * @param waveId 波次id
     * @param basketNo 篮号
     * @see SecondarySortingDTO.BasketDetail
     */
    List<SecondarySortingDTO.BasketDetail> basketDetail(String waveId, String basketNo);
    /**
     * 打印配货单
     *
     * @param waveId     波次编号
     * @see SecondarySortingDTO.BasketDetail
     */
    String printDistribution(String waveId, HttpServletResponse response);
    /**
     * 重置
     * @param waveId 波次id
     * @see SecondarySortingDTO.BasketDetail
     */
    SecondarySortingDTO.ScanCodeView reset(String waveId);
}
