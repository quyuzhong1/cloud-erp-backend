package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.dto.ProductBomHistoryDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.vo.BomVersionVO;

import java.util.List;

/**
 * bom 历史表(ProductBomHistory)表服务接口
 *
 * @author yl
 * @since 2023-01-11 14:04:50
 */
public interface ProductBomHistoryService  extends IService<ProductBomHistoryEntity> {


    void insert(BomInfoEntity bom, List<BomSkuDTO> bomSkuList);

    void deleteByBomId(String id);

    List<BomVersionVO> getVersionList(String id);

    List<ProductBomHistoryEntity> listByBomId(String bomId);
    /**
     * @description: 根据sku查询历史版本
     * @author Will
     * @date: 2023/8/28 19:01
     * @param dto
     * @return List<VersionDTO>
     */
    List<ProductBomHistoryDTO.VersionDTO> listHistoryVersion(ProductBomHistoryDTO.ParamDTO dto);

    /**
     * 保存bom 审核通过的的历史数据
     * @author yl
     * @date 2023-10-11 18:50
     * @param bom
     * @return void
     */
    void saveBomApprovalHistory(BomInfoEntity bom);
    /**
     * @description:
     * @author Will
     * @date: 2023/3/9 10:04
     * @return Boolean
     */
    Boolean updateSyncKingdeeStatus(String id ,String syncKingdeeStatus,String syncKingdeeId);
}
