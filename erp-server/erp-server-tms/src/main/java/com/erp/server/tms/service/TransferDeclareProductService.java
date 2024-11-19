package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.TransferDeclareProductDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.entity.TransferDeclareProductEntity;

import java.util.List;

/**
 * <p>
 * 中转报关产品 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
 */
public interface TransferDeclareProductService extends SuperService<TransferDeclareProductEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-01-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareProductDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-01-27
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareProductDTO.UpdateDTO dto);


    /**
     * 根据报关单详情id查询产品信息
     * @Author Luo_WG
     * @Date 2024/1/27 18:23
     * @param declareDetailIds
     * @return java.util.List<com.erp.model.tms.entity.TransferDeclareProductEntity>
     **/
    List<TransferDeclareProductEntity> listByDeclareDetailIds(List<String> declareDetailIds);

    /**
     * 根据报关单主表id查询产品信息
     * @Author Luo_WG
     * @Date 2024/1/27 18:27
     * @param declareIds
     * @return java.util.List<com.erp.model.tms.entity.TransferDeclareProductEntity>
     **/
    List<TransferDeclareProductEntity> listByDeclareIds(List<String> declareIds);


    void saveOrUpdateTransferDeclareProducts(List<TransferDeclareDetailEntity> transferDeclareDetailEntities);

    /**
     * 根据报关明细id删除拆分记录
     * @param deleteIds
     */
    void removeByDeclareDetailIds(List<String> deleteIds);
}
