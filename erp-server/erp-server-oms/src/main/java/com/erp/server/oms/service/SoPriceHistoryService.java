package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.entity.SoPriceHistoryEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
public interface SoPriceHistoryService extends SuperService<SoPriceHistoryEntity> {

    /**
     * 根据采购价目详情表id 获取历史数据
     * @author yl
     * @date 2023-03-29 10:28
     * @param priceDetailId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.HistoryDTO>
     */
    List<SoPriceDetailDTO.HistoryDTO> getHistory(String priceDetailId);

    /**
     * @description: 查询采购价目历史表报价
     * @author Will
     * @date: 2023/4/11 9:51
     * @param dto
     * @return List<SoTaxPriceViewDTO>
     */
    List<SoPriceDetailDTO.SoTaxPriceViewDTO> getHistoryTaxPrice(SoPriceDetailDTO.SoTaxPriceSearchDTO dto);

    /**
     * 方法说明
     * @author yl
     * @date 2023-04-11 14:50
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.AddDTO>
     */
    List<SoPriceDetailDTO.AddDTO> getBySupplierId(String supplierId,List<String> skuIdList);

    /**
     * 方法说明（批量）
     * @Author Luo_WG
     * @Date 2024/1/9 14:23
     * @param supplierIds
     * @param skuIdList
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.AddDTO>
     **/
    List<SoPriceDetailDTO.AddDTO> listBySupplierId(List<String> supplierIds,List<String> skuIdList);

    /**
     * 方法说明
     * @author yl
     * @date 2023-04-11 17:47
     * @param SoPriceDetailIds
     * @return java.util.List<com.erp.model.scm.entity.SoPriceHistoryEntity>
     */
    List<SoPriceHistoryEntity> getHistoryByDetailIds(List<String> SoPriceDetailIds);

    /**
     * 根据变更详情id 获取
     * @author yl
     * @date 2023-10-23 16:13
     * @param changeDetailIdList
     * @return java.util.List<com.erp.model.scm.entity.SoPriceHistoryEntity>
     */
    List<SoPriceHistoryEntity> listByChangeDetailIdList(List<String> changeDetailIdList);


    }
