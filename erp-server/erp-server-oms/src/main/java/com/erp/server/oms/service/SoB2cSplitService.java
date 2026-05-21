package com.erp.server.oms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.plm.enums.SkuStdSettingEnum;
import com.erp.model.plm.vo.SkuVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
     * 重置sku含税成本
     * @param soB2cEntity
     * @param detailEntity
     * @param skuVOList
     * @param childSkuList
     */
    void resetSkuVO(SoB2cEntity soB2cEntity, SoB2cDetailEntity detailEntity, List<SkuVO> skuVOList, List<String> childSkuList);

    /**
     * 重置sku含税成本
     * @param soB2cEntity
     * @param detailEntity
     * @param skuVO
     */
    void resetSkuVO(SoB2cEntity soB2cEntity, SoB2cDetailEntity detailEntity, SkuVO skuVO);

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

    SoB2cDTO.CombinationDTO listRefBomSplit(String detailId);

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
     * @param id
     * @param isCheckPlatform
     * @return BatchResultDTO
     * @description: 取消合并
     * @author Will
     * @date: 2023/8/21 9:24
     */
    BatchResultDTO cancelSplit(String id, Boolean isCheckPlatform);

    /**
     * 组合sku拆分数据
     *
     * @param soB2cEntity
     * @param detailEntityList
     * @param splitSkuDetailDTOS
     * @param skuNo
     * @return
     */
    SoB2cDTO.SplitSaveDTO buildSplitBySku(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> detailEntityList, List<SoB2cDTO.SplitSkuDetailDTO> splitSkuDetailDTOS, String skuNo);
}
