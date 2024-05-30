package com.erp.server.oms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;

/**
 * b2c订单拆分操作服务类
 */
public interface SoB2cSplitService extends SuperService<SoB2cEntity> {

    /**
     * 捆绑拆分信息
     * @param ids
     * @return
     */
    List<SoB2cDetailDTO.ViewDTO> getBomSplitInfo(List<String> ids);

    /**
     * 捆绑拆分
     * @param ids
     * @return
     */
    List<BatchResultDTO> bomSplitAndSave(List<String> ids);

    /**
     * 捆绑还原保存
     * @param ids
     * @return
     */
    List<BatchResultDTO> bomRestoreAndSave(List<String> ids);

    /**
     * 查询捆绑拆分后相同原始明细行
     * @param detailId
     * @return
     */
    List<SoB2cEntity> listRefBomSplit(String detailId);

    /**
     * 按照仓库进行订单拆分
     * @param ids
     * @return
     */
    List<BatchResultDTO> splitOrderByWarehouse(List<String> ids);

    /**
     * 页面查询BOM还原捆绑信息
     * @param id
     * @return
     */
    List<SoB2cDetailDTO.ViewDTO> getBomRestoreInfo(List<String> id);
    /**
     * @description: 拆分保存
     * @author Will
     * @date: 2023/8/21 9:23
     * @param dto
     * @return Boolean
     */
    SoB2cDTO.SplitSaveResultDTO splitSave(SoB2cDTO.SplitSaveDTO dto);

    /**
     * @description: 拆分显示
     * @author Will
     * @date: 2023/8/21 9:18
     * @param ids
     * @return ViewSplitDTO
     */
    List<SoB2cDTO.ViewSplitDTO> viewSplit(List<String> ids);

    /**
     * @description: 取消合并前数据展示
     * @author Will
     * @date: 2023/8/24 11:48
     * @param ids
     * @return List<CheckCancelSplitDTO>
     */
    List<SoB2cDTO.CheckCancelSplitDTO> checkCancelSplit(List<String> ids);

    /**
     * @description: 取消合并
     * @author Will
     * @date: 2023/8/21 9:24
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelSplit(String id);
}
