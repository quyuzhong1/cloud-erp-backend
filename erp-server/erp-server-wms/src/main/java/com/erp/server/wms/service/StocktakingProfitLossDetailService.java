package com.erp.server.wms.service;

import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.entity.StocktakingProfitLossDetailEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;

import java.util.List;

/**
 * <p>
 * 盘盈盘亏单详情 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
public interface StocktakingProfitLossDetailService extends SuperService<StocktakingProfitLossDetailEntity> {

    
    /**
     *根据主表id 获取到对应详情信息
     * @author yl
     * @date 2023-08-11 10:11
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.dto.StocktakingProfitLossDetailDTO.ViewDTO>
     */
    List<StocktakingProfitLossDetailDTO.ViewDTO> listByMainIds(List<String> mainIdList);

    /**
     * 修改 盘盈盘亏单详情
     * @param mainId
     * @param detailList
     */
    void updateInfo(String mainId, List<StocktakingProfitLossDetailDTO.UpdateDTO> detailList);

    /**
     * 根据主表id 删除数据
     * @author yl
     * @date 2023-10-20 14:23
     * @param id
     * @return void
     */
    void removeByMainId(String id);

    /**
     * 根据sourceId查询明细
     * @param sourceId
     * @return
     */
    List<StocktakingProfitLossDetailEntity> listBySourceId(String sourceId);
}
