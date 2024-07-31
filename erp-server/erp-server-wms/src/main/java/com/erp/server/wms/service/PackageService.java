package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.oms.dto.PackageDTO;

import java.util.List;

/**
 * @description
 * @return
 * @date 2024-01-30 11:06
 * @author Lambda
 */
public interface PackageService {

    /**
     * 扫描
     * @Author Luo_WG
     * @Date 2024/4/30 15:03
     * @param dto
     * @return com.erp.model.oms.dto.PackageDTO.ScanResultDTO
     **/
    PackageDTO.ScanResultDTO packageScan(PackageDTO.ScanDTO dto);

    /**
     * 合并组包
     * @param dto
     * @return
     */
    List<BatchResultDTO> mergePackage(PackageDTO.MergePackageDTO dto);
    /**
     * 查询发货单的称重重量
     * @author will
     * @date 2024/7/1 10:35
     * @param dto
     * @return WeightDTO
     */
    PackageDTO.WeightDTO getOrderWeight(PackageDTO.WeightParamDTO dto);
}
