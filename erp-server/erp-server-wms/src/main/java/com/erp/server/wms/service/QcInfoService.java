package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.entity.QcInfoEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
public interface QcInfoService extends SuperService<QcInfoEntity> {


    /**
     * 质检信息 暂存
     * @author yl
     * @date 2023-04-19 10:11
     * @param billId
     * @param qcInfo
     * @return void
     */
    void add(String billId, QcInfoDTO.AddDTO qcInfo);

    
    /**
     * 获取到质检信息
     * @author yl
     * @date 2023-04-19 12:24
     * @param id
     * @return com.erp.model.wms.dto.QcInfoDTO.ViewDTO
     */
    QcInfoDTO.ViewDTO getByMainId(String id);

    /**
     * 根据采购订单id集合 获取到已质检的数量
     * @author yl
     * @date 2023-04-20 12:59
     * @param purOrderIds
     * @return java.util.List<com.erp.model.wms.dto.QcInfoDTO.QcQtyDTO>
     */
    List<QcInfoDTO.QcQtyDTO> getPurOrderIds(List<String> purOrderIds);

    /**
     * 方法说明
     * @author yl
     * @date 2023-04-20 15:57
     * @param ids
     * @return void
     */
    List<QcInfoEntity> getByMainIdList(List<String> ids);

    /**
     * 批量免检后 批量去更新 数量
     * @author yl
     * @date 2023-04-20 17:07
     * @param ids
     * @return void
     */
    void updateQcQty(List<String> ids);

    
    /**
     * 更新处理措施
     * @author yl
     * @date 2023-04-20 19:17
     * @param ids
     * @param handleModeDict
     * @return java.lang.Boolean
     */
    Boolean updateHandleMode(List<String> ids, String handleModeDict);

    
    /**
     * 根据质检单id集合 获取到一些需要入库的数据
     * @author yl
     * @date 2023-04-24 15:47
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.dto.QcInfoDTO.StockInDTO>
     */
    List<QcInfoDTO.StockInDTO> getStockIn(List<String> mainIdList);
}
