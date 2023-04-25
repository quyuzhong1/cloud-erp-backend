package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.model.wms.entity.QcResultEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
public interface QcResultService extends SuperService<QcResultEntity> {


    /**
     * 质检信息 暂存
     * @author yl
     * @date 2023-04-19 10:11
     * @param billId
     * @param qcInfo
     * @return void
     */
    void add(String billId, QcResultDTO.AddDTO qcInfo);

    
    /**
     * 获取到质检信息
     * @author yl
     * @date 2023-04-19 12:24
     * @param id
     * @return com.erp.model.wms.dto.QcInfoDTO.ViewDTO
     */
    QcResultDTO.ViewDTO getByMainId(String id);

    /**
     * 根据采购订单id集合 获取到已质检的数量
     * @author yl
     * @date 2023-04-20 12:59
     * @param purOrderIds
     * @return java.util.List<com.erp.model.wms.dto.QcInfoDTO.QcQtyDTO>
     */
    List<QcResultDTO.QcQtyDTO> getPurOrderIds(List<String> purOrderIds);

    /**
     * 方法说明
     * @author yl
     * @date 2023-04-20 15:57
     * @param ids
     * @return void
     */
    List<QcResultEntity> getByMainIdList(List<String> ids);

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
    List<QcResultDTO.StockInDTO> getStockIn(List<String> mainIdList);

   /**
    * 根据质检单id 集合 获取删除数据
    * @author yl
    * @date 2023-04-25 16:15
    * @param mainIdList
    * @return void
    */
    void removeByMainIds(List<String> mainIdList);
}
